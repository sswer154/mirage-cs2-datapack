package com.sandrone.cs2grenade;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 医疗用品：长按右键读条，完成时回血、清负面效果，并按物品类型决定是否共享冷却。
 * 施法期间会锁住移动（或禁止奔跑），中断要经过 1 秒前摇。
 */
public final class SurgeryKit {

    /** 中断前摇。 */
    public static final int CANCEL_TICKS = 20;

    /** 每种医疗用品的参数。 */
    public static final class Profile {
        final String util;
        final int channelTicks;
        final int cooldownTicks;
        final float heal;
        final int uses;
        final boolean standStill;
        final int regenTicks;
        final String soundStart;
        final String soundEnd;

        Profile(String util, int channelTicks, int cooldownTicks, float heal, int uses,
                boolean standStill, int regenTicks, String soundStart, String soundEnd) {
            this.util = util;
            this.channelTicks = channelTicks;
            this.cooldownTicks = cooldownTicks;
            this.heal = heal;
            this.uses = uses;
            this.standStill = standStill;
            this.regenTicks = regenTicks;
            this.soundStart = soundStart;
            this.soundEnd = soundEnd;
        }
    }

    /** 已注册的医疗用品：键是物品 custom_data 里的 cs2_util。 */
    private static final Map<String, Profile> PROFILES = new HashMap<>();

    static {
        // 野战手术包：8 秒、不能移动、回 6 点、15 次、冷却 6 秒
        PROFILES.put("surgery_kit", new Profile("surgery_kit", 8 * 20, 6 * 20, 6.0f, 15, true, 0,
                "surgery.draw", "surgery.putaway"));
        // 金星：5.6 秒、可以移动但不能奔跑、回 1 点、8 次、冷却 3 秒、附带 10 秒生命恢复
        PROFILES.put("golden_star", new Profile("golden_star", 112, 3 * 20, 1.0f, 8, false, 10 * 20,
                "medical.zvezda_draw", "medical.zvezda_putaway"));
    }

    private static final ResourceLocation SPEED_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath("cs2", "surgery_kit_lock");
    private static final ResourceLocation SPRINT_PENALTY_ID =
            ResourceLocation.fromNamespaceAndPath("cs2", "medical_no_sprint");
    private static final ResourceLocation JUMP_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath("cs2", "surgery_kit_jump_lock");

    /** 手术包的九段音效，按读条进度播放（tick 偏移 -> 音效名）。 */
    private static final int[][] SURGERY_SOUNDS = {
            {20, 1}, {32, 2}, {64, 3}, {72, 4}, {88, 5}, {112, 6}, {120, 7}, {136, 8}, {156, 9}
    };

    private static final class Channel {
        final ServerPlayer player;
        final UUID uuid;
        final Profile profile;
        final int slot;
        final int startTick;
        int cancelAt;
        int lastClickTick;
        int usesLeft;
        java.util.Set<Integer> playedSounds = new java.util.HashSet<>();

        Channel(ServerPlayer player, Profile profile, int slot, int startTick) {
            this.player = player;
            this.uuid = player.getUUID();
            this.profile = profile;
            this.slot = slot;
            this.startTick = startTick;
        }
    }

    private static final Map<UUID, Channel> CHANNELS = new HashMap<>();
    private static final Map<UUID, Integer> COOLDOWNS = new HashMap<>();
    /** 所有见过「医疗用品」标签的物品类型，冷却共享。 */
    private static final java.util.Set<net.minecraft.world.item.Item> MEDICAL_ITEMS = new java.util.HashSet<>();
    /** 刚切到医疗用品时的 1 秒前摇。 */
    private static final Map<UUID, Integer> DRAW_READY = new HashMap<>();
    private static final Map<UUID, Boolean> WAS_HOLDING = new HashMap<>();

    private SurgeryKit() {
    }

    /** 物品对应的医疗用品参数，不认识就返回 null。 */
    private static Profile profileOf(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return null;
        }
        return PROFILES.get(data.copyTag().getString("cs2_util"));
    }

    public static boolean isMedical(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && "medical".equals(data.copyTag().getString("cs2_type"));
    }

    public static int remainingUses(ItemStack stack) {
        Profile p = profileOf(stack);
        int max = p == null ? 15 : p.uses;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return max;
        }
        CompoundTag tag = data.copyTag();
        return tag.contains("cs2_uses") ? tag.getInt("cs2_uses") : max;
    }

    /** 右键开始施法。返回 true 表示已接管这次点击。 */
    public static boolean tryStart(ServerPlayer player, ItemStack stack, int nowTick) {
        // 使用中再按一次右键：进入 1 秒打断前摇
        Channel running = CHANNELS.get(player.getUUID());
        if (running != null) {
            // 长按时客户端每 4 tick 左右会重发一次点击，间隔小于 10 tick 一律当成按住
            int gap = nowTick - running.lastClickTick;
            running.lastClickTick = nowTick;
            if (gap >= 10 && running.cancelAt == 0) {
                running.cancelAt = nowTick + CANCEL_TICKS;
                playSound(player, running.profile.soundEnd);
            }
            return true;
        }
        // 刚拿出来的 1 秒前摇
        Integer drawAt = DRAW_READY.get(player.getUUID());
        if (drawAt != null && nowTick < drawAt) {
            player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                    "§e拔出中 " + bar(drawAt - nowTick, CANCEL_TICKS, "\u00A7e")), true);
            return true;
        }
        Integer readyAt = COOLDOWNS.get(player.getUUID());
        if (readyAt != null && nowTick < readyAt) {
            return true;
        }
        Profile profile = profileOf(stack);
        if (profile == null) {
            return false;
        }
        int slot = player.getInventory().selected;
        // 只要开始使用就扣一次，即使之后被打断
        int usesLeft = remainingUses(stack) - 1;
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putInt("cs2_uses", Math.max(0, usesLeft));
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        stack.setDamageValue(Math.max(0, profile.uses - Math.max(0, usesLeft)));

        Channel ch = new Channel(player, profile, slot, nowTick);
        ch.usesLeft = usesLeft;
        ch.lastClickTick = nowTick;
        CHANNELS.put(player.getUUID(), ch);
        playSound(player, profile.soundStart);
        lockMovement(player, profile.standStill, true);
        return true;
    }

    public static void tick(net.minecraft.server.MinecraftServer server, int nowTick) {
        // 切到医疗用品的 1 秒前摇提示
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            boolean holding = profileOf(p.getMainHandItem()) != null;
            boolean was = WAS_HOLDING.getOrDefault(p.getUUID(), false);
            if (holding && !was && !CHANNELS.containsKey(p.getUUID())) {
                DRAW_READY.put(p.getUUID(), nowTick + CANCEL_TICKS);
            }
            WAS_HOLDING.put(p.getUUID(), holding);
            Integer drawAt = DRAW_READY.get(p.getUUID());
            if (holding && drawAt != null && nowTick < drawAt && !CHANNELS.containsKey(p.getUUID())) {
                p.displayClientMessage(net.minecraft.network.chat.Component.literal(
                        "§e拔出中 " + bar(drawAt - nowTick, CANCEL_TICKS, "\u00A7e")), true);
            }
        }
        if (CHANNELS.isEmpty()) {
            return;
        }
        List<UUID> finished = new ArrayList<>();
        for (Channel ch : CHANNELS.values()) {
            ServerPlayer player = ch.player;
            if (player.hasDisconnected() || player.isDeadOrDying() || player.isRemoved()) {
                lockMovement(player, ch.profile.standStill, false);
                finished.add(ch.uuid);
                continue;
            }
            // 施法期间锁死快捷栏
            if (player.getInventory().selected != ch.slot) {
                player.getInventory().selected = ch.slot;
                if (player.connection != null) {
                    player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket(ch.slot));
                }
                if (ch.cancelAt == 0) {
                    ch.cancelAt = nowTick + CANCEL_TICKS;
                    playSound(player, ch.profile.soundEnd);
                }
            }
            ItemStack held = player.getMainHandItem();
            boolean holdingMed = profileOf(held) == ch.profile;
            if (ch.profile.standStill) {
                player.setJumping(false);
                if (player.getDeltaMovement().y > 0) {
                    player.setDeltaMovement(player.getDeltaMovement().x, 0, player.getDeltaMovement().z);
                }
            } else {
                // 可以走，但不能跑
                player.setSprinting(false);
            }
            if ("golden_star".equals(ch.profile.util) && ch.cancelAt == 0) {
                int gt = nowTick - ch.startTick;
                if (gt == 8 && ch.playedSounds.add(8)) {
                    playSound(player, "medical.zvezda_open1");
                } else if (gt == 26 && ch.playedSounds.add(26)) {
                    playSound(player, "medical.zvezda_open2");
                } else if (gt == 48 && ch.playedSounds.add(48)) {
                    playSound(player, "medical.zvezda_use");
                }
            }
            // 手术包：按时间轴铺音效
            if ("surgery_kit".equals(ch.profile.util) && ch.cancelAt == 0) {
                int elapsed = nowTick - ch.startTick;
                for (int[] cue : SURGERY_SOUNDS) {
                    if (elapsed >= cue[0] && ch.playedSounds.add(cue[0])) {
                        playSound(player, String.format(java.util.Locale.ROOT, "surgery.s%02d", cue[1]));
                    }
                }
            }
            // 进度条
            if (ch.cancelAt > 0) {
                int passed = CANCEL_TICKS - Math.max(0, ch.cancelAt - nowTick);
                player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                        "§c打断中 " + bar(passed, CANCEL_TICKS, "\u00A7c") + " §f"
                                + oneDecimal(Math.max(0, ch.cancelAt - nowTick) / 20.0) + "s"), true);
            } else {
                int passed = nowTick - ch.startTick;
                player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                        "§a治疗中 " + bar(passed, ch.profile.channelTicks, "\u00A7a") + " §f"
                                + oneDecimal(passed / 20.0) + "s §7(右键取消)"), true);
            }
            if (!holdingMed && ch.cancelAt == 0) {
                ch.cancelAt = nowTick + CANCEL_TICKS;
                playSound(player, ch.profile.soundEnd);
            }
            if (ch.cancelAt > 0 && nowTick >= ch.cancelAt) {
                lockMovement(player, ch.profile.standStill, false);
                dropIfExhausted(player, ch);
                COOLDOWNS.put(player.getUUID(), nowTick + ch.profile.cooldownTicks);
                if (profileOf(player.getMainHandItem()) != null) {
                    applySharedCooldown(player, player.getMainHandItem(), ch.profile.cooldownTicks);
                }
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c已打断"), true);
                finished.add(ch.uuid);
                continue;
            }
            if (ch.cancelAt == 0 && nowTick - ch.startTick >= ch.profile.channelTicks) {
                complete(player, held, ch, nowTick);
                dropIfExhausted(player, ch);
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§a治疗完成"), true);
                finished.add(ch.uuid);
            }
        }
        for (UUID id : finished) {
            CHANNELS.remove(id);
        }
    }

    private static void complete(ServerPlayer player, ItemStack stack, Channel ch, int nowTick) {
        lockMovement(player, ch.profile.standStill, false);
        Profile p = profileOf(stack);
        if (p != ch.profile) {
            return;
        }
        player.heal(ch.profile.heal);
        for (MobEffectInstance effect : new ArrayList<>(player.getActiveEffects())) {
            if (!effect.getEffect().value().isBeneficial()) {
                player.removeEffect(effect.getEffect());
            }
        }
        if (ch.profile.regenTicks > 0) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, ch.profile.regenTicks, 0, false, false, true));
        }
        playSound(player, ch.profile.soundEnd);
        COOLDOWNS.put(player.getUUID(), nowTick + ch.profile.cooldownTicks);
        applySharedCooldown(player, stack, ch.profile.cooldownTicks);
    }

    private static void dropIfExhausted(ServerPlayer player, Channel ch) {
        if (ch.usesLeft > 0) {
            return;
        }
        ItemStack held = player.getMainHandItem();
        if (profileOf(held) == ch.profile && remainingUses(held) <= 0) {
            player.getInventory().removeItem(held);
        }
    }

    private static void applySharedCooldown(ServerPlayer player, ItemStack used, int ticks) {
        MEDICAL_ITEMS.add(used.getItem());
        for (net.minecraft.world.item.Item it : MEDICAL_ITEMS) {
            player.getCooldowns().addCooldown(it, ticks);
        }
    }

    private static void lockMovement(ServerPlayer player, boolean standStill, boolean lock) {
        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            boolean has = speed.getModifier(SPEED_MODIFIER_ID) != null;
            if (lock && standStill && !has) {
                speed.addTransientModifier(new AttributeModifier(SPEED_MODIFIER_ID, -1.0,
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            } else if ((!lock || !standStill) && has) {
                speed.removeModifier(SPEED_MODIFIER_ID);
            }
        }
        // 允许移动但不允许跑：把速度压掉三成，疾跑加成刚好被抵消
        AttributeInstance sprintPenalty = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (sprintPenalty != null) {
            boolean hasPenalty = sprintPenalty.getModifier(SPRINT_PENALTY_ID) != null;
            if (lock && !standStill && !hasPenalty) {
                sprintPenalty.addTransientModifier(new AttributeModifier(SPRINT_PENALTY_ID, -0.3,
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            } else if ((!lock || standStill) && hasPenalty) {
                sprintPenalty.removeModifier(SPRINT_PENALTY_ID);
            }
        }
        AttributeInstance jump = player.getAttribute(Attributes.JUMP_STRENGTH);
        if (jump != null) {
            boolean hasJump = jump.getModifier(JUMP_MODIFIER_ID) != null;
            if (lock && standStill && !hasJump) {
                jump.addTransientModifier(new AttributeModifier(JUMP_MODIFIER_ID, -1.0,
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            } else if ((!lock || !standStill) && hasJump) {
                jump.removeModifier(JUMP_MODIFIER_ID);
            }
        }
    }

    /** 自定义音效只存在于资源包，必须直接发声音包，走注册表会拿到空 Holder 并崩服。 */
    private static void playSound(ServerPlayer player, String id) {
        if (id == null || id.isEmpty() || player.connection == null) {
            return;
        }
        Cs2Grenade.debug("播放医疗音效 {} ({})", id, player.getScoreboardName());
        var loc = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("cs2", id);
        var sound = net.minecraft.sounds.SoundEvent.createVariableRangeEvent(loc);
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundPacket(
                net.minecraft.core.Holder.direct(sound), net.minecraft.sounds.SoundSource.PLAYERS,
                player.getX(), player.getY(), player.getZ(), 0.9f, 1.0f,
                player.level().getRandom().nextLong()));
    }

    private static String bar(int passed, int total, String color) {
        int segments = 20;
        int filled = Math.max(0, Math.min(segments, (int) Math.round(segments * (double) passed / total)));
        StringBuilder sb = new StringBuilder();
        sb.append(color);
        for (int i = 0; i < segments; i++) {
            sb.append(i < filled ? '■' : '□');
        }
        return sb.toString();
    }

    private static String oneDecimal(double value) {
        return String.format(java.util.Locale.ROOT, "%.1f", value);
    }

    /** 该玩家是否正在读条。 */
    public static boolean isChanneling(UUID uuid) {
        return CHANNELS.containsKey(uuid);
    }

    /** 外部触发的中断（比如丢物品）。返回 true 表示已接管。 */
    public static boolean requestCancel(ServerPlayer player, int nowTick) {
        Channel ch = CHANNELS.get(player.getUUID());
        if (ch == null || ch.cancelAt != 0) {
            return ch != null;
        }
        ch.cancelAt = nowTick + CANCEL_TICKS;
        playSound(player, ch.profile.soundEnd);
        return true;
    }

    public static void clearAll() {
        for (Channel ch : new ArrayList<>(CHANNELS.values())) {
            lockMovement(ch.player, ch.profile.standStill, false);
        }
        CHANNELS.clear();
        COOLDOWNS.clear();
        DRAW_READY.clear();
        WAS_HOLDING.clear();
    }
}
