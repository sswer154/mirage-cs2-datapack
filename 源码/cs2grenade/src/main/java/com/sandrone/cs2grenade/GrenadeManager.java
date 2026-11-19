/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.commands.CommandSourceStack
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Holder
 *  net.minecraft.core.Position
 *  net.minecraft.core.Vec3i
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundSoundPacket
 *  net.minecraft.network.protocol.game.ClientboundStopSoundPacket
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.ServerScoreboard
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.entity.Display$ItemDisplay
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.component.CustomData
 *  net.minecraft.world.item.component.CustomModelData
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.minecraft.world.phys.shapes.CollisionContext
 *  net.minecraft.world.scores.Objective
 *  net.minecraft.world.scores.ScoreHolder
 */
package com.sandrone.cs2grenade;

import com.sandrone.cs2grenade.Cs2Grenade;
import com.sandrone.cs2grenade.GrenadeLoader;
import com.sandrone.cs2grenade.GrenadeType;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreHolder;

public final class GrenadeManager {
    private static final ResourceKey<DamageType> FIRE_DAMAGE_TYPE = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("cs2", "fire"));
    private static final List<Grenade> ACTIVE = new ArrayList<Grenade>();
    private static final String GRENADE_TAG = "cs2_grenade_visual";
    private static MinecraftServer serverRef;
    private static final Map<String, PendingMask> PENDING_MASKS;
    private static int sweepTimer;
    private static boolean purgedAfterStart;
    private static final Map<UUID, Integer> DRAW_READY;
    private static final Map<UUID, PendingThrow> PENDING_THROW;
    private static final Map<UUID, String> LAST_HELD;
    private static final Set<UUID> PERSIST_DISPLAYS;
    private static final List<SmokeZone> SMOKE_ZONES;
    private static int smokeVolumeId;
    private static final List<SmokeCard> SMOKE_CARDS;
    private static final List<FireZone> FIRE_ZONES;
    private static final Map<UUID, Integer> BURN_STEPS;
    /** 每个受害者的火区叠层，按各自停留时间结算，不跟火区建成时刻绑定。 */
    private static final Map<UUID, FireVictim> FIRE_VICTIMS = new HashMap<UUID, FireVictim>();
    /** 火区一跳的间隔（tick），对应 0.5 秒。 */
    private static final int FIRE_HIT_INTERVAL = 10;
    /** 离开火区超过这个 tick 数就清掉叠层，防止贴着边缘进出无限重置。 */
    private static final int FIRE_STAY_GRACE = 10;

    private GrenadeManager() {
    }

    private static boolean isGrenadeVisual(Entity e) {
        if (!(e instanceof Display.ItemDisplay)) {
            return false;
        }
        Display.ItemDisplay d = (Display.ItemDisplay)e;
        ItemStack stack = d.getSlot(0).get();
        if (stack.isEmpty()) {
            return false;
        }
        CustomModelData cmd = (CustomModelData)stack.get(DataComponents.CUSTOM_MODEL_DATA);
        return cmd != null && (cmd.value() == 722001 || cmd.value() == 722002 || cmd.value() == 722003 || cmd.value() == 722004);
    }

    public static void cleanupOrphans(MinecraftServer server) {
        serverRef = server;
        int entities = 0;
        int fires = 0;
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity e : level.getAllEntities()) {
                if (GrenadeManager.isOrphanVisual(e)) {
                    e.discard();
                    ++entities;
                }
            }
            fires += GrenadeManager.cleanupTrackedFire(level);
        }
        if (entities > 0 || fires > 0) {
            Cs2Grenade.LOGGER.info("[cs2] 启动清理残留投掷物实体 {} 个、火方块 {} 个", entities, fires);
        }
    }

    /**
     * 区块加载时清理上一局残留。重启后区块往往还没加载，只靠 getAllEntities 会漏；
     * 这里按标签即时清掉，且不会误删仍在 ACTIVE / 烟区 / 持久展示里的对象。
     */
    public static void cleanupChunk(ServerLevel level, LevelChunk chunk) {
        int entities = 0;
        AABB chunkBounds = new AABB(
                chunk.getPos().getMinBlockX(),
                level.getMinBuildHeight(),
                chunk.getPos().getMinBlockZ(),
                chunk.getPos().getMaxBlockX() + 1,
                level.getMaxBuildHeight(),
                chunk.getPos().getMaxBlockZ() + 1);
        for (Entity e : level.getEntitiesOfClass(Entity.class, chunkBounds)) {
            if (!e.isRemoved() && GrenadeManager.isOrphanVisual(e)) {
                e.discard();
                ++entities;
            }
        }
        int fires = 0;
        FireBlockTracker tracker = FireBlockTracker.get(level);
        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;
        for (long packed : tracker.snapshot()) {
            BlockPos pos = BlockPos.of(packed);
            if ((pos.getX() >> 4) != chunkX || (pos.getZ() >> 4) != chunkZ) continue;
            if (level.getBlockState(pos).is(Blocks.FIRE)) {
                level.removeBlock(pos, false);
                ++fires;
            }
            tracker.remove(pos);
        }
        if (entities > 0 || fires > 0) {
            Cs2Grenade.debug("区块清理残留投掷物实体 {} 个、火方块 {} 个", entities, fires);
        }
    }

    private static boolean isOrphanVisual(Entity e) {
        if (e.isRemoved()) {
            return false;
        }
        if (e.getTags().contains(GRENADE_TAG) || GrenadeManager.isGrenadeVisual(e)) {
            for (Grenade g : ACTIVE) {
                if (g.display == e) return false;
            }
            return !PERSIST_DISPLAYS.contains(e.getUUID());
        }
        if (e.getTags().contains("cs2_smoke_canister")) {
            for (SmokeZone z : SMOKE_ZONES) {
                if (z.display == e) return false;
            }
            return true;
        }
        if (e.getTags().contains("cs2_smoke_card")) {
            for (SmokeZone z : SMOKE_ZONES) {
                if (e.getTags().contains("cs2_smoke_" + z.volumeId)) return false;
            }
            return true;
        }
        return false;
    }

    private static int cleanupTrackedFire(ServerLevel level) {
        FireBlockTracker tracker = FireBlockTracker.get(level);
        int removed = 0;
        for (long packed : tracker.snapshot()) {
            BlockPos pos = BlockPos.of(packed);
            if (level.getBlockState(pos).is(Blocks.FIRE)) {
                level.removeBlock(pos, false);
                ++removed;
            }
            tracker.remove(pos);
        }
        return removed;
    }

    public static boolean throwGrenade(Player player, ItemStack stack, int heldTicks) {
        Level level = player.level();
        if (!(level instanceof ServerLevel)) {
            return false;
        }
        ServerLevel level2 = (ServerLevel)level;
        CustomData data = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
        if (data == null || !data.contains("cs2_util")) {
            return false;
        }
        String kind = data.copyTag().getString("cs2_util");
        ResourceLocation typeId = kind.contains(":") ? ResourceLocation.parse((String)kind) : ResourceLocation.fromNamespaceAndPath((String)"cs2", (String)kind);
        GrenadeLoader.ensureLoaded(player.getServer());
        GrenadeType type = GrenadeLoader.get(typeId);
        if (type == null) {
            Cs2Grenade.LOGGER.warn("\u6ca1\u6709\u627e\u5230\u6295\u63b7\u7269\u914d\u7f6e {}\uff0c\u6309\u9ed8\u8ba4\u53c2\u6570\u6295\u51fa", (Object)typeId);
            type = GrenadeType.fallback(typeId);
        }
        if (!player.isCreative()) {
            stack.shrink(1);
        }
        Vec3 look = player.getLookAngle();
        double speed = type.speed();
        if (player.isShiftKeyDown()) {
            speed = type.speed() * 0.45;
        }
        Vec3 pos = player.getEyePosition().add(look.scale(0.4));
        ItemStack model = new ItemStack((ItemLike)Items.SNOWBALL);
        model.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(type.displayCmd() > 0 ? type.displayCmd() : type.modelCmd()));
        Display.ItemDisplay display = new Display.ItemDisplay(EntityType.ITEM_DISPLAY, (Level)level2);
        display.setPos(pos.x, pos.y + type.renderOffsetY(), pos.z);
        display.getSlot(0).set(model);
        display.addTag(GRENADE_TAG);
        level2.addFreshEntity((Entity)display);
        Vec3 throwVel = look.scale(speed).add(player.getDeltaMovement().scale(type.inheritVelocity()));
        int fuse = Math.max(20, type.fuse() - heldTicks);
        ACTIVE.add(new Grenade(display, level2, player, throwVel, fuse, type));
        level2.sendParticles((ParticleOptions)ParticleTypes.SMOKE, pos.x, pos.y, pos.z, 2, 0.05, 0.05, 0.05, 0.01);
        return true;
    }

    /** 耳鸣：每个玩家一条，时长跟着黑屏走。 */
    private record Ring(int holdEnd, int fadeTicks, int nextTick, float volume, int fadeSec) {
    }

    private static final java.util.Map<java.util.UUID, Ring> RING_STATE = new java.util.HashMap<>();

    private static void tickRings() {
        if (RING_STATE.isEmpty() || serverRef == null) {
            return;
        }
        int now = serverRef.getTickCount();
        var it = RING_STATE.entrySet().iterator();
        while (it.hasNext()) {
            var e = it.next();
            Ring r = e.getValue();
            int end = r.holdEnd() + r.fadeTicks();
            if (now >= end) {
                Cs2Grenade.debug("耳鸣结束，停止播放 fadeSec={}", r.fadeSec());
                // 淡出素材可能比黑屏长，结束时要主动停掉，否则会一直响
                player4Stop(serverRef, e.getKey(), "flash.ring_long");
                if (r.fadeSec() > 0) {
                    player4Stop(serverRef, e.getKey(), "flash.ring_fade_" + r.fadeSec());
                }
                it.remove();
                continue;
            }
            if (now < r.nextTick()) {
                continue;
            }
            var player = serverRef.getPlayerList().getPlayer(e.getKey());
            if (player == null) {
                it.remove();
                continue;
            }
            boolean fading = now >= r.holdEnd();
            float fade = 1.0f;
            if (fading && r.fadeTicks() > 0) {
                fade = Math.max(0.0f, 1.0f - (now - r.holdEnd()) / (float) r.fadeTicks());
            }
            if (!fading) {
                if (r.nextTick() == 0) {
                    // 保持段：一整段 10 秒素材，不循环，没有接缝
                    player.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundEntityPacket(
                            net.minecraft.core.Holder.direct(SoundEvent.createVariableRangeEvent(
                                    ResourceLocation.fromNamespaceAndPath("cs2", "flash.ring_long"))),
                            SoundSource.PLAYERS, player,
                            r.volume() * 0.5f, 1.0f, net.minecraft.util.RandomSource.create().nextLong()));
                    // 下一次唤醒点设在保持段结束，那一步才切进淡出
                    e.setValue(new Ring(r.holdEnd(), r.fadeTicks(), r.holdEnd(), r.volume(), 0));
                }
            } else {
                if (r.nextTick() != -2) {
                    player.connection.send(new net.minecraft.network.protocol.game.ClientboundStopSoundPacket(
                            ResourceLocation.fromNamespaceAndPath("cs2", "flash.ring_long"), SoundSource.PLAYERS));
                    int sec = Math.max(1, Math.min(5, Math.round(r.fadeTicks() / 20.0f)));
                    Cs2Grenade.debug("耳鸣进入淡出: fadeTicks={} 使用{}秒档", r.fadeTicks(), sec);
                    player.connection.send(new net.minecraft.network.protocol.game.ClientboundSoundEntityPacket(
                            net.minecraft.core.Holder.direct(SoundEvent.createVariableRangeEvent(
                                    ResourceLocation.fromNamespaceAndPath("cs2", "flash.ring_fade_" + sec))),
                            SoundSource.PLAYERS, player,
                            r.volume() * 0.5f, 1.0f, net.minecraft.util.RandomSource.create().nextLong()));
                    e.setValue(new Ring(r.holdEnd(), r.fadeTicks(), -2, r.volume(), sec));
                }
            }
        }
    }

    /** 停掉该玩家身上可能残留的所有耳鸣声实例，避免多颗闪光叠成一条噪音。 */
    private static void stopRingSounds(ServerPlayer player) {
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundStopSoundPacket(
                ResourceLocation.fromNamespaceAndPath("cs2", "flash.ring_long"), SoundSource.PLAYERS));
        for (int sec = 1; sec <= 5; sec++) {
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundStopSoundPacket(
                    ResourceLocation.fromNamespaceAndPath("cs2", "flash.ring_fade_" + sec), SoundSource.PLAYERS));
        }
    }

    private static void player4Stop(MinecraftServer server, java.util.UUID uuid, String id) {
        var pl = server.getPlayerList().getPlayer(uuid);
        if (pl == null || pl.connection == null) {
            return;
        }
        pl.connection.send(new net.minecraft.network.protocol.game.ClientboundStopSoundPacket(
                ResourceLocation.fromNamespaceAndPath("cs2", id), SoundSource.PLAYERS));
    }

    private static void processMasks() {
        if (PENDING_MASKS.isEmpty() || serverRef == null) {
            return;
        }
        int now = serverRef.getTickCount();
        CommandSourceStack src = serverRef.createCommandSourceStack().withPermission(4).withSuppressedOutput();
        ArrayList<String> done = new ArrayList<String>();
        for (PendingMask m : List.copyOf(PENDING_MASKS.values())) {
            if (now < m.expireTick()) continue;
            serverRef.getCommands().performPrefixedCommand(src, "execute as " + m.player() + " run media mask " + m.color() + " 0 " + m.fadeTicks() + " @s");
            done.add(m.player());
        }
        done.forEach(PENDING_MASKS::remove);
    }

    public static void tick() {
        tickRings();
        GrenadeManager.processMasks();
        GrenadeManager.tickSmoke();
        GrenadeManager.tickSmokeCards();
        GrenadeManager.tickFires();
        if (++sweepTimer >= 40) {
            sweepTimer = 0;
            GrenadeManager.sweepOrphans();
        }
        if (ACTIVE.isEmpty()) {
            return;
        }
        ACTIVE.removeIf(g -> {
            GrenadeManager.step(g);
            return g.fuse <= 0;
        });
    }

    private static String utilOf(ItemStack stack) {
        CustomData data = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return null;
        }
        String util = data.copyTag().getString("cs2_util");
        return util.isEmpty() ? null : util;
    }

    public static void playerTick(ServerPlayer player, int now) {
        String held = GrenadeManager.utilOf(player.getMainHandItem());
        String last = LAST_HELD.get(player.getUUID());
        boolean isGrenade = false;
        if (held != null) {
            ResourceLocation loc = held.contains(":")
                    ? ResourceLocation.parse(held)
                    : ResourceLocation.fromNamespaceAndPath("cs2", held);
            isGrenade = GrenadeLoader.get(loc) != null;
        }
        if (isGrenade && !held.equals(last)) {
            DRAW_READY.put(player.getUUID(), now + 10);
            player.getCooldowns().addCooldown(player.getMainHandItem().getItem(), 10);
            GrenadeManager.playCs2((ServerLevel)player.level(), player.position(), "grenade.draw", 0.6f, 1.0f);
        }
        if (!isGrenade) {
            PENDING_THROW.remove(player.getUUID());
        }
        LAST_HELD.put(player.getUUID(), held == null ? "" : held);
        PendingThrow pt = PENDING_THROW.get(player.getUUID());
        if (pt == null || now < pt.endTick()) {
            return;
        }
        PENDING_THROW.remove(player.getUUID());
        if (player.getInventory().selected != pt.slot() || !pt.util().equals(GrenadeManager.utilOf(player.getMainHandItem()))) {
            return;
        }
        if (GrenadeManager.throwGrenade((Player)player, player.getMainHandItem(), 0)) {
            int pick = 1 + ThreadLocalRandom.current().nextInt(3);
            GrenadeManager.playCs2((ServerLevel)player.level(), player.position(), "grenade.fuze_" + pick, 0.55f, 1.0f);
        }
    }

    public static boolean requestThrow(ServerPlayer player) {
        int now = serverRef == null ? 0 : serverRef.getTickCount();
        Integer ready = DRAW_READY.get(player.getUUID());
        if (ready != null && now < ready) {
            return true;
        }
        if (PENDING_THROW.containsKey(player.getUUID())) {
            return true;
        }
        String util = GrenadeManager.utilOf(player.getMainHandItem());
        if (util == null) {
            return false;
        }
        PENDING_THROW.put(player.getUUID(), new PendingThrow(now + 6, player.getInventory().selected, util));
        GrenadeManager.playCs2((ServerLevel)player.level(), player.position(), "grenade.pin", 0.7f, 1.0f);
        return true;
    }

    public static void purgeTransientVisuals(MinecraftServer server) {
        if (server == null) {
            return;
        }
        for (ServerLevel level : server.getAllLevels()) {
            int before = 0;
            ArrayList<Entity> doomed = new ArrayList<Entity>();
            for (Entity e : level.getAllEntities()) {
                if (!e.getTags().contains("cs2_smoke_card") && !e.getTags().contains("cs2_smoke_canister")) continue;
                doomed.add(e);
            }
            for (Entity e : doomed) {
                e.discard();
                ++before;
            }
            if (before <= 0) continue;
            Cs2Grenade.debug("\u6e05\u7406\u6b8b\u7559\u70df\u7247 {} \u4e2a", before);
        }
        SMOKE_ZONES.clear();
        SMOKE_CARDS.clear();
    }

    private static void sweepOrphans() {
        HashSet<UUID> alive = new HashSet<UUID>();
        for (Grenade g : ACTIVE) {
            if (g.display == null) continue;
            alive.add(g.display.getUUID());
        }
        alive.addAll(PERSIST_DISPLAYS);
        for (ServerLevel level : serverRef.getAllLevels()) {
            HashSet<String> activeSmoke = new HashSet<String>();
            HashSet<UUID> activeCanisters = new HashSet<UUID>();
            for (SmokeZone z : SMOKE_ZONES) {
                if (z.level != level) continue;
                activeSmoke.add("cs2_smoke_" + z.volumeId);
                if (z.display != null && !z.display.isRemoved()) {
                    activeCanisters.add(z.display.getUUID());
                }
            }
            ArrayList<Entity> doomed = new ArrayList<Entity>();
            for (Entity e : level.getAllEntities()) {
                if (e.isRemoved()) continue;
                if (e.getTags().contains("cs2_smoke_canister")) {
                    if (!activeCanisters.contains(e.getUUID())) doomed.add(e);
                    continue;
                }
                if (e.getTags().contains("cs2_smoke_card")) {
                    if (java.util.Collections.disjoint(e.getTags(), activeSmoke)) doomed.add(e);
                    continue;
                }
                if (alive.contains(e.getUUID()) || !GrenadeManager.isGrenadeVisual(e)) continue;
                doomed.add(e);
            }
            for (Entity e : doomed) {
                e.discard();
            }
            if (!doomed.isEmpty()) {
                Cs2Grenade.debug("清理无主手雷/烟片/罐体 {} 个", doomed.size());
            }
        }
    }

    private static boolean inMatch(ServerPlayer player) {
        if (serverRef == null) {
            return false;
        }
        ServerScoreboard board = serverRef.getScoreboard();
        Objective obj = board.getObjective("cs2_inmatch");
        if (obj == null) {
            return false;
        }
        return board.getOrCreatePlayerScore((ScoreHolder)player, obj).get() == 1;
    }

    private static boolean isEnemy(Player source, LivingEntity victim) {
        if (source == null || !(victim instanceof Player target) || target == source) {
            return false;
        }
        var sourceTeam = source.getTeam();
        var victimTeam = target.getTeam();
        return sourceTeam != null && victimTeam != null && !sourceTeam.getName().equals(victimTeam.getName());
    }

    private static void addDamageScore(Player source, String objectiveName, float amount) {
        if (!(source instanceof ServerPlayer player) || amount <= 0.0f || serverRef == null) {
            return;
        }
        if (!GrenadeManager.inMatch(player)) {
            return;
        }
        Objective objective = serverRef.getScoreboard().getObjective(objectiveName);
        if (objective == null) {
            return;
        }
        serverRef.getScoreboard().getOrCreatePlayerScore((ScoreHolder)player, objective).add(Math.round(amount));
    }

    /** C4 安放开始音：给附近玩家。 */
    static void playPlantSound(ServerLevel level, Vec3 pos) {
        GrenadeManager.playCs2(level, pos, "c4.plant_start", 1.0f, 1.0f);
    }

    /** C4 拆除开始音：给附近玩家。 */
    static void playDefuseSound(ServerLevel level, Vec3 pos) {
        GrenadeManager.playCs2(level, pos, "c4.defuse_start", 1.0f, 1.0f);
    }

    /** 数据包通过计分板信号触发的 C4 音效。 */
    static void playC4Sfx(ServerPlayer player, Vec3 pos, String id, float volume) {
        ResourceLocation loc = ResourceLocation.fromNamespaceAndPath("cs2", id);
        SoundEvent sound = SoundEvent.createVariableRangeEvent(loc);
        GrenadeManager.sendSoundPacket(player, sound, pos, volume, 1.0f,
                player.getServer().overworld().getRandom().nextLong());
    }

    /** 中断时掐掉安放开始音。 */
    static void stopPlantSound(ServerLevel level, Vec3 pos) {
        GrenadeManager.stopSound(level, pos, "c4.plant_start");
    }

    /** 拆除/爆炸时掐掉还在播的倒计时。 */
    static void stopC4Countdown(ServerLevel level, Vec3 pos) {
        GrenadeManager.stopSound(level, pos, "c4.countdown");
    }

    /** 全服掐掉倒计时：拆除函数会先杀掉炸弹标记，不能靠坐标找。 */
    static void stopC4CountdownAll() {
        if (serverRef == null) {
            return;
        }
        ResourceLocation loc = ResourceLocation.fromNamespaceAndPath("cs2", "c4.countdown");
        for (ServerPlayer p : serverRef.getPlayerList().getPlayers()) {
            p.connection.send(new ClientboundStopSoundPacket(loc, SoundSource.PLAYERS));
        }
    }

    private static void playCs2(ServerLevel level, Vec3 pos, String id, float volume, float pitch) {
        ResourceLocation loc = ResourceLocation.fromNamespaceAndPath((String)"cs2", (String)id);
        SoundEvent sound = SoundEvent.createVariableRangeEvent((ResourceLocation)loc);
        Cs2Grenade.debug("\u64ad\u653e\u6295\u63b7\u97f3\u6548 {} vol={} pitch={} at {} {} {}", id, volume, pitch, (int)pos.x, (int)pos.y, (int)pos.z);
        double range = 16.0 * (double)volume;
        for (ServerPlayer p : level.players()) {
            if (p.distanceToSqr(pos) > range * range) continue;
            GrenadeManager.sendSoundPacket(p, sound, pos, volume, pitch, level.getRandom().nextLong());
        }
    }

    /** 只给单个玩家停某条自定义音效。 */
    static void stopSoundFor(ServerPlayer p, String id) {
        if (p.connection == null) return;
        ResourceLocation loc = ResourceLocation.fromNamespaceAndPath("cs2", id);
        p.connection.send(new ClientboundStopSoundPacket(loc, SoundSource.PLAYERS));
    }

    /** 只给单个玩家播自定义音效。 */
    static void playCs2For(ServerPlayer p, ServerLevel level, Vec3 pos, String id, float volume, float pitch) {
        ResourceLocation loc = ResourceLocation.fromNamespaceAndPath("cs2", id);
        SoundEvent sound = SoundEvent.createVariableRangeEvent(loc);
        GrenadeManager.sendSoundPacket(p, sound, pos, volume, pitch, level.getRandom().nextLong());
    }

    private static void stopSound(ServerLevel level, Vec3 pos, String id) {
        ResourceLocation loc = ResourceLocation.fromNamespaceAndPath((String)"cs2", (String)id);
        for (ServerPlayer p : level.players()) {
            if (p.distanceToSqr(pos) > 4096.0) continue;
            p.connection.send((Packet)new ClientboundStopSoundPacket(loc, SoundSource.PLAYERS));
        }
    }

    private static void sendSoundPacket(ServerPlayer p, SoundEvent sound, Vec3 pos, float volume, float pitch, long seed) {
        if (p.connection == null) {
            return;
        }
        p.connection.send(new ClientboundSoundPacket(Holder.direct(sound), SoundSource.PLAYERS, pos.x, pos.y, pos.z, volume, pitch, seed));
    }

    private static void playHit(Grenade g, Vec3 pos, String id, float volume, float pitch) {
        int now = serverRef == null ? 0 : serverRef.getTickCount();
        Cs2Grenade.debug("\u78b0\u649e\u89e6\u53d1 type={} effect={} speed={} \u8ddd\u4e0a\u6b21={}", g.type.id(), g.type.effect(), g.vel.length(), now - g.lastSoundTick);
        volume = Math.max(0.8f, volume * 2.6f);
        if (now - g.lastSoundTick < 3) {
            return;
        }
        g.lastSoundTick = now;
        String effect = g.type.effect();
        String group = "collide_he";
        int variants = 3;
        if ("flash".equals(effect)) {
            group = "impact_flash";
            variants = 3;
        } else if ("smoke".equals(effect) || "fire".equals(effect)) {
            // 燃烧弹也是玻璃瓶，和烟雾弹同属轻质罐体，用同一组轻碰音
            group = "impact_smoke";
            variants = 3;
        }
        int pick = 1 + ThreadLocalRandom.current().nextInt(variants);
        double speed = g.vel.length();
        if ("collide_he".equals(group) && speed < 0.22) {
            pick = 1 + ThreadLocalRandom.current().nextInt(3);
            id = "cs2:grenade.collide_short_" + pick;
        } else {
            id = "cs2:grenade." + group + "_" + pick;
        }
        BlockState state = g.level.getBlockState(BlockPos.containing((Position)pos));
        String mat = state.getSoundType().toString().toLowerCase(Locale.ROOT);
        float matPitch = pitch;
        if (mat.contains("metal")) {
            matPitch *= 1.15f;
        } else if (mat.contains("wood")) {
            matPitch *= 1.3f;
        } else if (mat.contains("gravel")) {
            matPitch *= 0.9f;
        } else if (mat.contains("grass")) {
            matPitch *= 0.8f;
        }
        pitch = matPitch;
        ResourceLocation loc = ResourceLocation.parse((String)id);
        SoundEvent sound = SoundEvent.createVariableRangeEvent((ResourceLocation)loc);
        double range = 16.0 * (double)volume;
        for (ServerPlayer p : g.level.players()) {
            if (p.distanceToSqr(pos) > range * range) continue;
            GrenadeManager.sendSoundPacket(p, sound, pos, volume, pitch, g.level.getRandom().nextLong());
        }
    }

    private static void step(Grenade g) {
        Vec3 from = g.lastPos;
        if (!g.level.isLoaded(BlockPos.containing(from))) {
            return;
        }
        Vec3 previousVel = g.vel;
        g.vel = g.vel.add(0.0, -g.type.gravity(), 0.0).scale(g.type.drag());
        Vec3 to = from.add(g.vel);
        if (!g.level.isLoaded(BlockPos.containing(to))) {
            g.vel = previousVel;
            return;
        }
        BlockHitResult hit = g.level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)g.thrower));
        if (hit.getType() == HitResult.Type.BLOCK) {
            int nowTick;
            Vec3 normal = Vec3.atLowerCornerOf((Vec3i)hit.getDirection().getNormal());
            double impactSpeed = g.vel.length();
            g.vel = g.vel.subtract(normal.scale(2.0 * g.vel.dot(normal))).scale(g.type.bounce());
            to = hit.getLocation().add(normal.scale(0.02));
            ++g.bounces;
            if ("fire".equals(g.type.effect()) && normal.y > 0.7) {
                g.fuse = 0;
                GrenadeManager.explode(g, hit.getLocation());
                return;
            }
            if ("impact".equals(g.type.effect()) && g.travel >= 4.0) {
                g.fuse = 0;
                GrenadeManager.explode(g, hit.getLocation());
                return;
            }
            int n = nowTick = serverRef == null ? 0 : serverRef.getTickCount();
            if (!g.soundTailPlayed && impactSpeed > 0.08 && nowTick >= g.wallSoundEndTick) {
                GrenadeManager.playHit(g, hit.getLocation(), g.type.hitSoundWall(), g.type.hitVolumeWall(), g.type.hitPitchWall());
                g.soundStarted = true;
                g.soundPos = hit.getLocation();
                g.wallSoundEndTick = nowTick + 5;
            }
        }
        if (g.soundStarted && !g.soundTailPlayed && g.tailPendingAt == 0) {
            if (g.vel.length() < 0.12) {
                ++g.restTicks;
                if (g.restTicks >= 3) {
                    int nowTick;
                    int n = nowTick = serverRef == null ? 0 : serverRef.getTickCount();
                    if (g.wallSoundEndTick > nowTick) {
                        g.tailPendingAt = g.wallSoundEndTick;
                    } else {
                        GrenadeManager.playHit(g, g.lastPos, g.type.hitSoundTail(), g.type.hitVolume() * 0.7f, g.type.hitPitch());
                        g.soundTailPlayed = true;
                    }
                }
            } else {
                g.restTicks = 0;
            }
        }
        if (g.tailPendingAt > 0 && serverRef != null && serverRef.getTickCount() >= g.tailPendingAt) {
            GrenadeManager.playHit(g, g.lastPos, g.type.hitSoundTail(), g.type.hitVolume() * 0.7f, g.type.hitPitch());
            g.soundTailPlayed = true;
            g.tailPendingAt = 0;
        }
        g.travel += from.distanceTo(to);
        Vec3 after = to;
        if (g.display.horizontalCollision || g.display.verticalCollision) {
            Vec3 v = g.vel;
            if (g.display.verticalCollision) {
                v = new Vec3(v.x, -v.y * 0.35, v.z);
            }
            if (g.display.horizontalCollision) {
                v = new Vec3(-v.x * 0.35, v.y, v.z);
            }
            g.vel = v.scale(0.9);
            to = after;
        }
        g.lastPos = to;
        if (g.display != null && g.display.isAlive()) {
            g.display.setPos(to.x, to.y + g.type.renderOffsetY(), to.z);
        }
        if (g.fuse % 2 == 0) {
            g.level.sendParticles((ParticleOptions)ParticleTypes.SMOKE, to.x, to.y, to.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
        if ("smoke".equals(g.type.effect())) {
            // 烟雾弹没有引信：停稳或接触火才起烟
            if (g.vel.length() < 0.12) {
                ++g.smokeRest;
            } else {
                g.smokeRest = 0;
            }
            net.minecraft.core.BlockPos at = net.minecraft.core.BlockPos.containing(to);
            boolean burning = g.level.getBlockState(at).is(net.minecraft.world.level.block.Blocks.FIRE)
                    || g.level.getBlockState(at).is(net.minecraft.world.level.block.Blocks.LAVA)
                    || inFireZone(g.level, at);
            // 烟雾弹也要递减兜底引信，避免卡在未加载区块或水面时永不起烟
            --g.fuse;
            if (g.smokeRest >= 3 || burning || g.fuse <= 0) {
                g.fuse = 0;
            }
        } else if (--g.fuse == 20) {
            // empty if block
        }
        if (g.fuse <= 0) {
            GrenadeManager.explode(g, to);
        }
    }

    private static void tickSmokeCards() {
        if (SMOKE_CARDS.isEmpty() || serverRef == null) {
            return;
        }
        int now = serverRef.getTickCount();
        ArrayList<SmokeCard> done = new ArrayList<SmokeCard>();
        for (SmokeCard c : SMOKE_CARDS) {
            int elapsed = now - c.startTick;
            if (elapsed < 0) continue;
            if (elapsed > c.growTicks + 40) {
                done.add(c);
                continue;
            }
            if (elapsed % 10 != 0) continue;
            double p = Math.min(1.0, (double)elapsed / (double)Math.max(1, c.growTicks));
            double ease = 1.0 - Math.pow(1.0 - p, 2.2);
            Vec3 cur = c.from.add(c.to.subtract(c.from).scale(ease));
            float scale = (float)((double)c.scale * (0.35 + 0.65 * ease));
            CommandSourceStack src = c.level.getServer().createCommandSourceStack().withPermission(4).withSuppressedOutput();
            c.level.getServer().getCommands().performPrefixedCommand(src, String.format(Locale.ROOT, "execute as @e[tag=%s] run tp @s %.3f %.3f %.3f", c.tag, cur.x, cur.y, cur.z));
            c.level.getServer().getCommands().performPrefixedCommand(src, String.format(Locale.ROOT, "execute as @e[tag=%s] run data modify entity @s transformation.scale set value [%.3ff,%.3ff,%.3ff]", c.tag, Float.valueOf(scale), Float.valueOf(scale), Float.valueOf(scale)));
        }
        SMOKE_CARDS.removeAll(done);
    }

    private static void spawnSmokeCards(ServerLevel level, Vec3 pos, double radius, int id) {
        RandomSource random = level.random;
        CommandSourceStack src = level.getServer().createCommandSourceStack().withPermission(4).withSuppressedOutput();
        Vec3 center = pos.add(0.0, 0.6, 0.0);
        // 确保烟片生成的区块已加载；远距离投掷时落点区块可能还没到手
        level.getChunkAt(BlockPos.containing(center));
        int[] ringCounts = new int[]{1, 10, 24, 42, 60};
        double[] ringRadii = new double[]{0.0, 0.25, 0.5, 0.75, 1.0};
        float[] ringScaleBoost = new float[]{1.0f, 1.04f, 1.03f, 1.02f, 1.0f};
        float baseScale = 7.6f;
        int index = 0;
        for (int ring = 0; ring < ringCounts.length; ++ring) {
            int n = ringCounts[ring];
            double ringR = radius * ringRadii[ring];
            for (int i = 0; i < n; ++i) {
                double z;
                double y;
                double x;
                if (ringR <= 0.01) {
                    x = center.x;
                    y = center.y + 0.2;
                    z = center.z;
                } else {
                    double phi = Math.acos(1.0 - 2.0 * ((double)i + 0.5) / (double)n);
                    double theta = Math.PI * (1.0 + Math.sqrt(5.0)) * ((double)i + 0.5);
                    double sinPhi = Math.sin(phi);
                    x = center.x + Math.cos(theta) * sinPhi * ringR;
                    y = center.y + Math.cos(phi) * ringR * 0.85;
                    z = center.z + Math.sin(theta) * sinPhi * ringR;
                }
                float scale = (float)((double)baseScale * (1.0 - 0.45 * ringRadii[ring]) * (double)ringScaleBoost[ring]);
                int modelCmd = 723001 + index % 4;
                String cardTag = String.format(Locale.ROOT, "cs2_card_%d_%d", id, index);
                String cmd = String.format(Locale.ROOT, "summon minecraft:item_display " + center.x + " " + center.y + " " + center.z + " {Tags:[\"cs2_smoke_card\",\"cs2_smoke_%d\",\"%s\"],item:{id:\"minecraft:paper\",count:1,components:{\"minecraft:custom_model_data\":%d}},brightness:{block:15,sky:15},transformation:{translation:[0f,0f,0f],left_rotation:[0f,0f,0f,1f],scale:[%.3ff,%.3ff,%.3ff],right_rotation:[0f,0f,0f,1f]},billboard:\"center\",teleport_duration:10,interpolation_duration:10}", id, cardTag, modelCmd, (double)scale * 0.35, (double)scale * 0.35, (double)scale * 0.35);
                level.getServer().getCommands().performPrefixedCommand(src, cmd);
                SMOKE_CARDS.add(new SmokeCard(level, cardTag, center, new Vec3(x, y, z), scale, (serverRef == null ? 0 : serverRef.getTickCount()) + 2, 20));
                ++index;
            }
        }
    }

    /** 落点是否在已铺开的火区里（燃烧瓶自己铺的火也算）。 */
    private static boolean inFireZone(ServerLevel level, net.minecraft.core.BlockPos pos) {
        for (FireZone z : FIRE_ZONES) {
            if (z.level != level) {
                continue;
            }
            Vec3 at = Vec3.atCenterOf(pos);
            for (FireNode node : z.nodes) {
                if (node.pos.distanceTo(at) <= 2.0) {
                    return true;
                }
            }
            if (z.center.distanceTo(at) <= 2.0) {
                return true;
            }
        }
        return false;
    }

    private static void igniteFire(Grenade g, Vec3 pos) {
        ServerLevel level = g.level;
        int life = 140;
        int startTick = serverRef == null ? 0 : serverRef.getTickCount();
        FireZone zone = new FireZone(level, pos, startTick, life, g.thrower);
        FIRE_ZONES.add(zone);
        // 立刻铺一次火，别等下一 tick 的 tickFires：落点是不完整方块时也要当场有火，
        // 不能出现“弹体已删、火区为空、20 tick 后安静消失”的空窗
        placeFire(zone, 4.0);
        zone.lastPlaceTick = startTick;
        level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.8f, 1.1f);
        if (g.display != null && !g.display.isRemoved()) {
            g.display.discard();
        }
    }

    private static final double FIRE_NODE_RADIUS = 0.8;
    private static final double FIRE_VERTICAL_TOLERANCE = 0.65;
    /** 支撑面最小厚度（格）：1/16 即可，铁轨 2/16、活板门 3/16、地毯 1/16 这些不完整方块都算支撑面。 */
    private static final double FIRE_SUPPORT_MIN_TOP = 0.0625;
    /** 落点正下方找不到火格时，向四周补找支撑面的最大水平半径。 */
    private static final int FIRE_NEARBY_RADIUS = 2;

    private static void placeFire(FireZone zone, double radius) {
        zone.nodes.clear();
        int cx = (int)Math.floor(zone.center.x);
        int cz = (int)Math.floor(zone.center.z);
        int baseY = (int)Math.floor(zone.center.y);
        BlockPos centerGround = findFireCenter(zone.level, cx, cz, baseY);
        if (centerGround == null) {
            // 落点正下方没有能直接放火的格（薄片方块、碰撞体内部等），先在附近找最近的支撑面
            centerGround = findFireCenterNearby(zone.level, cx, cz, baseY);
        }
        if (centerGround == null) {
            // 实在找不到支撑面也要留一个火节点：宁可火悬在落点，也不能静默空掉
            zone.nodes.add(new FireNode(zone.center, (float)FIRE_NODE_RADIUS));
            return;
        }
        int centerY = centerGround.getY();
        // 以“沿地面走过的格数”限制蔓延，而不是两点直线距离
        int maxSteps = Math.max(2, (int)Math.ceil(radius));
        ArrayDeque<BlockPos> queue = new ArrayDeque<BlockPos>();
        HashMap<BlockPos, Integer> depth = new HashMap<BlockPos, Integer>();
        HashSet<BlockPos> visited = new HashSet<BlockPos>();
        visited.add(centerGround);
        depth.put(centerGround, 0);
        queue.add(centerGround);
        while (!queue.isEmpty()) {
            BlockPos cur = queue.poll();
            int d = depth.getOrDefault(cur, 0);
            addFireNode(zone, cur);
            if (d >= maxSteps) {
                continue;
            }
            BlockPos[] neighbors = new BlockPos[]{
                cur.offset(1, 0, 0), cur.offset(-1, 0, 0),
                cur.offset(0, 0, 1), cur.offset(0, 0, -1)
            };
            for (BlockPos n : neighbors) {
                BlockPos next = findFireGround(zone.level, n.getX(), n.getZ(), cur.getY());
                if (next == null) continue;
                int dy = next.getY() - cur.getY();
                if (dy < -1 || dy > 1) continue;
                if (next.getY() > centerY + 1) continue;
                if (!fireHasHeadroom(zone.level, next)) continue;
                if (!fireSpreadPathAllowed(zone.level, cur, next)) continue;
                if (visited.add(next)) {
                    depth.put(next, d + 1);
                    queue.add(next);
                }
            }
        }
    }

    private static BlockPos findFireCenter(ServerLevel level, int cx, int cz, int baseY) {
        for (int y = baseY + 2; y >= baseY - 4; --y) {
            BlockPos at = new BlockPos(cx, y, cz);
            if (isFireCell(level, at)) {
                return at;
            }
        }
        return null;
    }

    /** 落点这一列放不下火时，在水平方向最多 FIRE_NEARBY_RADIUS 格内找最近的可用火格。 */
    private static BlockPos findFireCenterNearby(ServerLevel level, int cx, int cz, int baseY) {
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (int dx = -FIRE_NEARBY_RADIUS; dx <= FIRE_NEARBY_RADIUS; ++dx) {
            for (int dz = -FIRE_NEARBY_RADIUS; dz <= FIRE_NEARBY_RADIUS; ++dz) {
                if (dx == 0 && dz == 0) continue;
                double dist = (double)(dx * dx + dz * dz);
                if (dist >= bestDist) continue;
                BlockPos candidate = findFireCenter(level, cx + dx, cz + dz, baseY);
                if (candidate != null) {
                    best = candidate;
                    bestDist = dist;
                }
            }
        }
        return best;
    }

    private static BlockPos findFireGround(ServerLevel level, int x, int z, int fromY) {
        for (int y = fromY + 1; y >= fromY - 1; --y) {
            BlockPos at = new BlockPos(x, y, z);
            if (isFireCell(level, at)) {
                return at;
            }
        }
        return null;
    }

    private static boolean isFireCell(ServerLevel level, BlockPos pos) {
        if (!level.getBlockState(pos).isAir()) {
            return false;
        }
        BlockPos below = pos.below();
        VoxelShape support = level.getBlockState(below).getCollisionShape(level, below);
        return !support.isEmpty() && support.max(Direction.Axis.Y) >= FIRE_SUPPORT_MIN_TOP;
    }

    /** 火节点所在格子的支撑面世界高度；没有碰撞形状时退回格子底面。 */
    private static double fireSurfaceY(ServerLevel level, BlockPos cell) {
        BlockPos below = cell.below();
        VoxelShape support = level.getBlockState(below).getCollisionShape(level, below);
        return (double)below.getY() + (support.isEmpty() ? 0.0 : support.max(Direction.Axis.Y));
    }

    private static void addFireNode(FireZone zone, BlockPos cell) {
        Vec3 at = new Vec3((double)cell.getX() + 0.5, fireSurfaceY(zone.level, cell) + 0.05, (double)cell.getZ() + 0.5);
        if (GrenadeManager.smokeCovers(zone.level, at)) {
            return;
        }
        zone.nodes.add(new FireNode(at, (float)FIRE_NODE_RADIUS));
    }

    private static boolean fireHasHeadroom(ServerLevel level, BlockPos to) {
        for (int y = to.getY(); y <= to.getY() + 1; ++y) {
            BlockPos p = new BlockPos(to.getX(), y, to.getZ());
            if (!level.getBlockState(p).isAir()) {
                return false;
            }
        }
        return true;
    }

    private static boolean fireSpreadPathAllowed(ServerLevel level, BlockPos cur, BlockPos next) {
        // 只允许相邻格高度差 1 以内；上坡时在较高那层打射线（可越过 1 格台阶），平走/下坡在当前层挡住墙体
        int dy = next.getY() - cur.getY();
        if (dy > 1 || dy < -1) {
            return false;
        }
        // 射线高度取两边支撑面的较高者再抬 0.1：栅栏/铁轨这类比一格矮的支撑面上，
        // 用格子中心 +0.5 会正好擦着碰撞面，火就下不到旁边的地面
        double rayY = Math.max(fireSurfaceY(level, cur), fireSurfaceY(level, next)) + 0.1;
        Vec3 from = new Vec3(cur.getX() + 0.5, rayY, cur.getZ() + 0.5);
        Vec3 to = new Vec3(next.getX() + 0.5, rayY, next.getZ() + 0.5);
        BlockHitResult hit = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, CollisionContext.empty()));
        return hit.getType() == HitResult.Type.MISS;
    }

    private static boolean inFire(FireZone zone, LivingEntity e) {
        double ex = e.getX();
        double ey = e.getY();
        double ez = e.getZ();
        for (FireNode node : zone.nodes) {
            double dx = ex - node.pos.x;
            double dz = ez - node.pos.z;
            if (dx * dx + dz * dz > (double)node.radius * (double)node.radius) continue;
            if (Math.abs(ey - node.pos.y) > FIRE_VERTICAL_TOLERANCE) continue;
            return true;
        }
        return false;
    }

    /** 该位置是否在某个烟区的有效范围内（同维度、高度差 3 格内、水平在烟半径内）。 */
    private static boolean smokeCovers(ServerLevel level, Vec3 pos) {
        for (SmokeZone s : SMOKE_ZONES) {
            if (s.level != level) continue;
            // 烟大致是个球：离烟心 6 格以内（含高度）都算被盖住
            if (s.pos.distanceTo(pos) > s.maxRadius) continue;
            return true;
        }
        return false;
    }

    private static void extinguish(FireZone z) {
        z.nodes.clear();
    }

    private static void emitFireParticles(FireZone zone, double radius, int elapsed) {
        if (zone.nodes.isEmpty()) {
            return;
        }
        int remaining = Math.max(0, zone.lifeTicks - elapsed);
        int maxSamples = Math.min(16, Math.max(9, (int)Math.round(7.0 + radius)));
        if (remaining < 60) {
            maxSamples = Math.max(4, maxSamples * remaining / 60);
        }
        RandomSource random = zone.level.random;
        int samples = Math.min(maxSamples, zone.nodes.size());
        for (int i = 0; i < samples; ++i) {
            FireNode node = zone.nodes.get(random.nextInt(zone.nodes.size()));
            double x = node.pos.x + (random.nextDouble() - 0.5) * 0.45;
            double y = node.pos.y;
            double z = node.pos.z + (random.nextDouble() - 0.5) * 0.45;
            if ((i & 7) == 0) {
                zone.level.sendParticles(ParticleTypes.LAVA, x, y + 0.02, z, 1, 0.02, 0.02, 0.02, 0.0);
            }
            zone.level.sendParticles(ParticleTypes.FLAME, x, y + 0.08, z, 1, 0.04, 0.02, 0.04, 0.0);
            if ((i & 1) == 0) {
                zone.level.sendParticles(ParticleTypes.SMALL_FLAME, x, y + 0.18, z, 1, 0.04, 0.03, 0.04, 0.0);
            }
            if (i % 4 == 0) {
                zone.level.sendParticles(ParticleTypes.SMOKE, x, y + 0.5, z, 1, 0.02, 0.08, 0.02, 0.0);
            }
        }
    }

    private static void tickFires() {
        if (FIRE_ZONES.isEmpty() || serverRef == null) {
            return;
        }
        int now = serverRef.getTickCount();
        ArrayList<FireZone> dead = new ArrayList<FireZone>();
        HashMap<UUID, LivingEntity> inFireNow = new HashMap<UUID, LivingEntity>();
        HashMap<UUID, Player> fireSources = new HashMap<UUID, Player>();
        for (FireZone fireZone : FIRE_ZONES) {
            int elapsed = now - fireZone.startTick;
            if (elapsed > fireZone.lifeTicks) {
                GrenadeManager.extinguish(fireZone);
                dead.add(fireZone);
                continue;
            }
            int stage = Math.max(1, (elapsed + 10) / 20);
            double radius = 4.0 + Math.min(5.0, (double)(stage - 1));
            // 只熄灭落在烟范围内的火格，烟外的火继续烧
            fireZone.nodes.removeIf(node -> GrenadeManager.smokeCovers(fireZone.level, node.pos));
            if (fireZone.nodes.isEmpty() && now - fireZone.startTick > 20) {
                GrenadeManager.extinguish(fireZone);
                dead.add(fireZone);
                continue;
            }
            if (fireZone.nodes.isEmpty() || now - fireZone.lastPlaceTick >= 20) {
                GrenadeManager.placeFire(fireZone, radius);
                fireZone.lastPlaceTick = now;
            }
            if (elapsed % 2 == 0) {
                emitFireParticles(fireZone, radius, elapsed);
            }
            // 收集本 tick 处在火里的实体；伤害在循环外按每个人自己的停留节奏结算，
            // 这样后进火区的人不会直接吃到后面阶段的伤害，多火区也只算一跳
            for (LivingEntity e : fireZone.level.getEntitiesOfClass(LivingEntity.class, new AABB(fireZone.center, fireZone.center).inflate(radius + 1.0, 8.0, radius + 1.0))) {
                Player p;
                if (e instanceof Player && (p = (Player)e).isSpectator()) continue;
                double dx = e.getX() - fireZone.center.x;
                double dz = e.getZ() - fireZone.center.z;
                if (dx * dx + dz * dz > radius * radius) continue;
                if (!inFire(fireZone, e)) continue;
                inFireNow.putIfAbsent(e.getUUID(), e);
                Player source = fireZone.thrower;
                if (source != null) fireSources.putIfAbsent(e.getUUID(), source);
            }
            if (elapsed >= fireZone.lifeTicks) {
                GrenadeManager.extinguish(fireZone);
                dead.add(fireZone);
            }
        }
        // 火区伤害：从每个人自己进火区那 tick 起，每 FIRE_HIT_INTERVAL tick 结算一跳，
        // 伤害按停留时间 1 -> 2 -> 3 -> 4 封顶，与火区已经烧了多久无关
        for (Map.Entry<UUID, LivingEntity> entry : inFireNow.entrySet()) {
            LivingEntity victim = entry.getValue();
            if (victim.isRemoved() || victim.isDeadOrDying()) {
                FIRE_VICTIMS.remove(entry.getKey());
                continue;
            }
            FireVictim rec = FIRE_VICTIMS.get(entry.getKey());
            if (rec == null) {
                rec = new FireVictim(now);
                FIRE_VICTIMS.put(entry.getKey(), rec);
            }
            rec.lastSeenTick = now;
            Player source = fireSources.get(entry.getKey());
            if (source != null) rec.thrower = source;
            if (now < rec.nextHitTick) continue;
            float fireDamage = Math.min(4.0f, (float)(rec.stage + 1));
            victim.invulnerableTime = 0;
            if (victim.hurt(victim.level().damageSources().source(FIRE_DAMAGE_TYPE, rec.thrower), fireDamage) && isEnemy(rec.thrower, victim)) {
                GrenadeManager.addDamageScore(rec.thrower, "cs2_fire_dmg", fireDamage);
            }
            ++rec.stage;
            rec.nextHitTick = now + FIRE_HIT_INTERVAL;
        }
        FIRE_VICTIMS.entrySet().removeIf(entry -> now - entry.getValue().lastSeenTick > FIRE_STAY_GRACE);
        FIRE_ZONES.removeAll(dead);
    }

    private static void smokeExplode(Grenade g, Vec3 pos) {
        ServerLevel level = g.level;
        int life = Math.max(60, g.type.cloudSeconds() * 20);
        int volumeId = ++smokeVolumeId;
        GrenadeManager.spawnSmokeCards(level, pos, g.type.cloudRadius(), volumeId);
        int born = serverRef == null ? 0 : serverRef.getTickCount();
        SmokeZone zone = new SmokeZone(level, pos, born, life, g.type.cloudRadius(), 0, volumeId, g.display);
        zone.nextHiss = born + 140;
        SMOKE_ZONES.add(zone);
        GrenadeManager.playCs2(level, pos, "smoke.hiss", 0.8f, 1.0f);
        if (g.display != null && !g.display.isRemoved()) {
            PERSIST_DISPLAYS.add(g.display.getUUID());
            g.display.addTag("cs2_smoke_canister");
        }
    }

    private static void tickSmoke() {
        if (SMOKE_ZONES.isEmpty() || serverRef == null) {
            return;
        }
        int now = serverRef.getTickCount();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        ArrayList<SmokeZone> expired = new ArrayList<SmokeZone>();
        for (SmokeZone z : SMOKE_ZONES) {
            int elapsed = now - z.startTick;
            if (elapsed >= z.lifeTicks) {
                for (java.util.UUID id : z.hissTicks.keySet()) {
                    net.minecraft.server.level.ServerPlayer p = z.level.getServer().getPlayerList().getPlayer(id);
                    if (p != null) GrenadeManager.stopSoundFor(p, "smoke.hiss");
                }
                z.hissTicks.clear();
                z.level.getServer().getCommands().performPrefixedCommand(z.level.getServer().createCommandSourceStack().withPermission(4).withSuppressedOutput(), "kill @e[tag=cs2_smoke_" + z.volumeId + "]");
                if (z.display != null) {
                    PERSIST_DISPLAYS.remove(z.display.getUUID());
                    z.display.discard();
                }
                expired.add(z);
                continue;
            }
            // 放烟声按玩家维护：进烟区补一次，留在里面按样本续播；淡出后交给 smoke.end
            java.util.HashSet<java.util.UUID> seen = new java.util.HashSet<>();
            if (!z.fadedOut) {
            for (net.minecraft.server.level.ServerPlayer p : z.level.players()) {
                double ddx = p.getX() - z.pos.x;
                double ddz = p.getZ() - z.pos.z;
                double horiz2 = ddx * ddx + ddz * ddz;
                double limit = z.maxRadius + 1.5;
                if (horiz2 <= limit * limit && Math.abs(p.getY() - z.pos.y) <= 4.0) {
                    seen.add(p.getUUID());
                    Integer last = z.hissTicks.get(p.getUUID());
                    // hiss 样本 8 秒：在 152 tick（7.6 秒）后续播，接得上又不明显
                    if (last == null || now - last >= 152) {
                        GrenadeManager.playCs2For(p, z.level, z.pos, "smoke.hiss", 0.8f, 1.0f);
                        z.hissTicks.put(p.getUUID(), now);
                    }
                }
            }
            java.util.Iterator<java.util.Map.Entry<java.util.UUID, Integer>> it = z.hissTicks.entrySet().iterator();
            while (it.hasNext()) {
                java.util.Map.Entry<java.util.UUID, Integer> e = it.next();
                if (seen.contains(e.getKey())) continue;
                // 走远不掐声，也不清记录：8 秒内回来听到的还是同一段，超过就由上面的续播补
                it.remove();
            }
            }
            if (!z.fadedOut && elapsed >= z.lifeTicks - 60) {
                z.fadedOut = true;
                for (java.util.UUID id : z.hissTicks.keySet()) {
                    net.minecraft.server.level.ServerPlayer p = z.level.getServer().getPlayerList().getPlayer(id);
                    if (p != null) GrenadeManager.stopSoundFor(p, "smoke.hiss");
                }
                z.hissTicks.clear();
                GrenadeManager.playCs2(z.level, z.pos, "smoke.end", 0.8f, 1.0f);
            }
            double progress = (double)elapsed / (double)z.lifeTicks;
            double spread = z.maxRadius * (0.4 + 0.6 * progress);
            int remaining = z.lifeTicks - elapsed;
            if (remaining >= 60 || now < z.nextCore) continue;
            double f = Math.max(0.05, (double)remaining / 60.0);
            String scaleCmd = String.format(Locale.ROOT, "execute as @e[tag=cs2_smoke_%d] run data modify entity @s transformation.scale set value [%.3ff,%.3ff,%.3ff]", z.volumeId, 3.4 * f, 3.4 * f, 3.4 * f);
            z.level.getServer().getCommands().performPrefixedCommand(z.level.getServer().createCommandSourceStack().withPermission(4).withSuppressedOutput(), scaleCmd);
            z.nextCore = now + 10;
        }
        SMOKE_ZONES.removeAll(expired);
    }

    private static boolean fireFragment(ServerLevel level, Vec3 origin, Vec3 dir, float fragDamage, double fragInner, double fragOuter, Player thrower) {
        double segLen;
        Vec3 cur = origin;
        Vec3 d = dir.normalize();
        double remaining = fragOuter;
        for (int bounce = 0; bounce < 4 && remaining > 0.2; remaining -= segLen, ++bounce) {
            BlockHitResult blockHit = level.clip(new ClipContext(cur, cur.add(d.scale(remaining)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
            segLen = blockHit.getType() == HitResult.Type.MISS ? remaining : Math.max(0.2, blockHit.getLocation().distanceTo(cur) - 0.05);
            Vec3 segEnd = cur.add(d.scale(segLen));
            AABB segBox = new AABB(cur, segEnd).inflate(1.2);
            LivingEntity victim = null;
            double bestAlong = Double.MAX_VALUE;
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, segBox)) {
                double along;
                Player p;
                if (e instanceof Player && (p = (Player)e).isSpectator() || e.isDeadOrDying() || e.getHealth() <= 0.0f) continue;
                AABB box = e.getBoundingBox().inflate(0.3);
                if (box.contains(cur)) {
                    if (!(bestAlong > 0.0)) continue;
                    bestAlong = 0.0;
                    victim = e;
                    continue;
                }
                Optional clip = box.clip(cur, segEnd);
                if (clip.isEmpty() || !((along = ((Vec3)clip.get()).subtract(cur).dot(d)) < bestAlong)) continue;
                bestAlong = along;
                victim = e;
            }
            if (victim != null) {
                double dist = victim.position().distanceTo(origin);
                if (dist <= fragOuter) {
                    float factor = dist <= fragInner ? 1.0f : (float)(1.0 - 0.25 * (dist - fragInner) / (fragOuter - fragInner));
                    victim.invulnerableTime = 0;
                    if (victim.hurt(level.damageSources().explosion(thrower, null), fragDamage * factor) && isEnemy(thrower, victim)) {
                        GrenadeManager.addDamageScore(thrower, "cs2_grenade_dmg", fragDamage * factor);
                    }
                }
                return true;
            }
            if (blockHit.getType() == HitResult.Type.MISS) {
                return false;
            }
            Vec3 normal = Vec3.atLowerCornerOf((Vec3i)blockHit.getDirection().getNormal());
            d = d.subtract(normal.scale(2.0 * d.dot(normal))).normalize();
            cur = blockHit.getLocation().add(normal.scale(0.05));
        }
        return false;
    }

    private static void flashExplode(Grenade g, Vec3 pos) {
        ServerLevel level = g.level;
        double range = 40.0;
        for (ServerPlayer p : level.players()) {
            if (p.position().distanceTo(pos) > range) continue;
            level.sendParticles(p, (ParticleOptions)ParticleTypes.FLASH, true, pos.x, pos.y, pos.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
        java.util.Map<java.util.UUID, Double> flashPower = new java.util.HashMap<>();
        for (ServerPlayer pp : level.players()) {
            if (!GrenadeManager.inMatch(pp) || pp.position().distanceTo(pos) > range) {
                continue;
            }
            BlockHitResult h0 = level.clip(new ClipContext(pp.getEyePosition(), pos,
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)pp));
            boolean blk = h0.getType() == HitResult.Type.BLOCK
                    && !(h0.getLocation().distanceTo(pos) < 0.6);
            if (blk) {
                continue;
            }
            double dd = pp.position().distanceTo(pos);
            double df = dd <= 5.0 ? 1.0 : Math.max(0.2, 1.0 - 0.8 * (dd - 5.0) / (range - 5.0));
            Vec3 tf = pos.subtract(pp.getEyePosition()).normalize();
            double dt = tf.dot(pp.getLookAngle());
            double af = dt <= 0.0 ? 0.03 : dt * dt;
            double it = Math.min(1.0, df * af);
            if (it >= 0.005) {
                flashPower.put(pp.getUUID(), it);
            }
        }
        for (ServerPlayer p : level.players()) {
            double d = p.position().distanceTo(pos);
            if (d > 40.0) {
                continue;
            }
            // 被闪到的玩家也听得到这一发爆响，但按闪光强度衰减；耳鸣仍然盖在上面
            double ownPower = flashPower.getOrDefault(p.getUUID(), 0.0);
            String layer;
            float loud;
            float pitch;
            if (d < 10.0) {
                layer = "grenade.flash_bang";
                loud = 2.5f;
                pitch = 1.15f;
            } else if (d < 25.0) {
                layer = "flash.far";
                loud = 2.0f;
                pitch = 1.08f;
            } else {
                layer = "flash.distant";
                loud = 2.5f;
                pitch = 1.0f;
            }
            if (ownPower >= 0.01) {
                // 中闪越重，自己听到的爆响越轻，最低保留 0.6
                loud = Math.max(0.6f, (float) (loud * (1.0 - 0.7 * Math.min(1.0, ownPower))));
            }
            GrenadeManager.sendSoundPacket(p, SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath("cs2", layer)),
                    pos, loud, pitch, level.getRandom().nextLong());
        }
        for (ServerPlayer p : level.players()) {
            Vec3 toFlash;
            double dot;
            double angleFactor;
            double dist;
            double distFactor;
            double intensity;
            boolean blocked;
            if (!GrenadeManager.inMatch(p) || p.position().distanceTo(pos) > range) continue;
            BlockHitResult hit = level.clip(new ClipContext(p.getEyePosition(), pos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)p));
            boolean bl = blocked = hit.getType() == HitResult.Type.BLOCK;
            if (blocked && hit.getLocation().distanceTo(pos) < 0.6) {
                blocked = false;
            }
            if (blocked || (intensity = Math.min(1.0, (distFactor = (dist = p.position().distanceTo(pos)) <= 5.0 ? 1.0 : Math.max(0.2, 1.0 - 0.8 * (dist - 5.0) / (range - 5.0))) * (angleFactor = (dot = (toFlash = pos.subtract(p.getEyePosition()).normalize()).dot(p.getLookAngle())) <= 0.0 ? 0.03 : dot * dot))) < 0.005) continue;
            int alpha = 255;
            int ticks = Math.max(6, (int)Math.round((double)(g.type.maskSeconds() * 20) * intensity));
            int fadeTicks = Math.max(6, (int)Math.round(100.0 * intensity));
            // 同一玩家同一时刻只保留一条耳鸣声：先停掉上一颗闪光留下的实例，再登记新的。
            // 与黑屏遮罩一致——更弱/更短的一颗不覆盖当前这条，免得把长耳鸣截短。
            int ringNow = serverRef == null ? 0 : serverRef.getTickCount();
            Ring prevRing = RING_STATE.get(p.getUUID());
            int prevRemaining = prevRing == null ? 0 : prevRing.holdEnd() + prevRing.fadeTicks() - ringNow;
            if (prevRemaining <= 0 || ticks + fadeTicks > prevRemaining) {
                Cs2Grenade.debug("耳鸣登记 {} 强度={} 保持={}t 淡出={}t",
                        p.getScoreboardName(), String.format(Locale.ROOT, "%.2f", intensity), ticks, fadeTicks);
                stopRingSounds(p);
                RING_STATE.put(p.getUUID(), new Ring(ringNow + ticks, fadeTicks, 0,
                        (float) (0.35 + 0.85 * intensity), 0));
            }
            String who = p.getGameProfile().getName();
            PendingMask current = PENDING_MASKS.get(who);
            if (current != null) {
                int nowTick0 = serverRef == null ? 0 : serverRef.getTickCount();
                int remainingHold = current.expireTick() - nowTick0;
                if (remainingHold > 0 && ticks <= remainingHold) continue;
            }
            MinecraftServer server = level.getServer();
            CommandSourceStack src = server.createCommandSourceStack().withPermission(4).withSuppressedOutput();
            String maskCmd = "execute as " + who + " run media mask " + g.type.maskColor() + " " + alpha + " 0 @s";
            server.getCommands().performPrefixedCommand(src, maskCmd);
            if (ticks <= 0) continue;
            PENDING_MASKS.put(who, new PendingMask(who, g.type.maskColor(), server.getTickCount() + ticks, Math.max(20, (int)Math.round(100.0 * intensity)), alpha, intensity));
        }
        level.sendParticles((ParticleOptions)ParticleTypes.END_ROD, pos.x, pos.y, pos.z, 40, 0.6, 0.6, 0.6, 0.25);
        if (g.display != null) {
            g.display.discard();
        }
    }

    public static void explodeAt(ServerLevel level, Vec3 pos, Player thrower) {
        Grenade g = new Grenade(null, level, thrower, Vec3.ZERO, 0, GrenadeType.fallback(ResourceLocation.fromNamespaceAndPath((String)"cs2", (String)"he")));
        GrenadeManager.explode(g, pos);
    }

    private static void explode(Grenade g, Vec3 pos) {
        ServerLevel level = g.level;
        if ("flash".equals(g.type.effect())) {
            GrenadeManager.flashExplode(g, pos);
            return;
        }
        if ("smoke".equals(g.type.effect())) {
            GrenadeManager.smokeExplode(g, pos);
            return;
        }
        if ("fire".equals(g.type.effect())) {
            GrenadeManager.igniteFire(g, pos);
            return;
        }
        double pierceInner = g.type.pierceInner();
        double pierceOuter = g.type.pierceOuter();
        float pierceDamage = g.type.pierceDamage();
        AABB pierceBox = new AABB(pos, pos).inflate(pierceOuter);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, pierceBox)) {
            double dist;
            Player p;
            if (e instanceof Player && (p = (Player)e).isSpectator() || (dist = e.position().distanceTo(pos)) > pierceOuter) continue;
            float factor = dist <= pierceInner ? 1.0f : (float)(1.0 - (dist - pierceInner) / (pierceOuter - pierceInner));
            if (e.hurt(level.damageSources().explosion(g.thrower, null), pierceDamage * factor) && isEnemy(g.thrower, e)) {
                GrenadeManager.addDamageScore(g.thrower, "cs2_grenade_dmg", pierceDamage * factor);
            }
        }
        int rays = g.type.fragRays();
        float fragDamage = g.type.fragDamage();
        double fragInner = g.type.fragInner();
        double fragOuter = g.type.fragOuter();
        RandomSource random = level.random;
        for (int r = 0; r < rays; ++r) {
            double theta = random.nextDouble() * Math.PI * 2.0;
            double cosPhi = random.nextDouble() * 2.0 - 1.0;
            double sinPhi = Math.sqrt(1.0 - cosPhi * cosPhi);
            Vec3 dir = new Vec3(Math.cos(theta) * sinPhi, cosPhi, Math.sin(theta) * sinPhi);
            GrenadeManager.fireFragment(level, pos, dir, fragDamage, fragInner, fragOuter, g.thrower);
        }
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, new AABB(pos, pos).inflate(fragOuter))) {
            Vec3 center;
            Vec3 base;
            Player p;
            if (target instanceof Player && (p = (Player)target).isSpectator() || (base = (center = target.position().add(0.0, (double)target.getBbHeight() * 0.5, 0.0)).subtract(pos)).lengthSqr() < 0.04) continue;
            base = base.normalize();
            for (int k = 0; k < 2; ++k) {
                Vec3 jitter = new Vec3(random.nextDouble() - 0.5, random.nextDouble() - 0.5, random.nextDouble() - 0.5).scale(0.12);
                GrenadeManager.fireFragment(level, pos, base.add(jitter).normalize(), fragDamage, fragInner, fragOuter, g.thrower);
            }
        }
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, pierceBox)) {
            double dist;
            Player p;
            if (e instanceof Player && (p = (Player)e).isSpectator() || (dist = e.position().distanceTo(pos)) > pierceOuter) continue;
            float f = dist <= 1.0 ? 1.0f : (float)(1.0 - dist / pierceOuter);
            Vec3 push = e.position().subtract(pos).normalize().scale((double)g.type.knockback() * Math.max(0.1, (double)f));
            e.push(push.x, push.y + 0.3 * Math.max(0.1, (double)f), push.z);
        }
        level.sendParticles((ParticleOptions)ParticleTypes.EXPLOSION, pos.x, pos.y, pos.z, 6, 0.3, 0.3, 0.3, 0.05);
        for (ServerPlayer p : level.players()) {
            if (p != g.thrower && !GrenadeManager.inMatch(p)) continue;
            level.sendParticles(p, (ParticleOptions)ParticleTypes.EXPLOSION_EMITTER, true, pos.x, pos.y, pos.z, 1, 0.0, 0.0, 0.0, 0.0);
            level.sendParticles(p, (ParticleOptions)ParticleTypes.EXPLOSION, true, pos.x, pos.y, pos.z, 4, 0.6, 0.6, 0.6, 0.05);
        }
        level.sendParticles((ParticleOptions)ParticleTypes.EXPLOSION_EMITTER, pos.x, pos.y, pos.z, 1, 0.0, 0.0, 0.0, 0.0);
        boolean fire = "fire".equals(g.type.effect());
        for (ServerPlayer p : level.players()) {
            double d = p.position().distanceTo(pos);
            if (d > 40.0) {
                continue;
            }
            String layer;
            if (d < 10.0) {
                layer = fire ? "grenade.explosion_fire_close" : "grenade.explosion_close";
            } else if (d < 25.0) {
                layer = "grenade.explosion_far";
            } else {
                layer = "grenade.explosion_distant";
            }
            float loud = 2.5f;
            SoundEvent sound = SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath("cs2", layer));
            GrenadeManager.sendSoundPacket(p, sound, pos, loud, 1.0f, level.getRandom().nextLong());
        }
        if (g.display != null) {
            g.display.discard();
        }
    }

    static {
        PENDING_MASKS = new HashMap<String, PendingMask>();
        sweepTimer = 0;
        purgedAfterStart = false;
        DRAW_READY = new HashMap<UUID, Integer>();
        PENDING_THROW = new HashMap<UUID, PendingThrow>();
        LAST_HELD = new HashMap<UUID, String>();
        PERSIST_DISPLAYS = new HashSet<UUID>();
        SMOKE_ZONES = new ArrayList<SmokeZone>();
        smokeVolumeId = 0;
        SMOKE_CARDS = new ArrayList<SmokeCard>();
        FIRE_ZONES = new ArrayList<FireZone>();
        BURN_STEPS = new HashMap<UUID, Integer>();
    }

    private static final class Grenade {
        final Display.ItemDisplay display;
        final ServerLevel level;
        final Player thrower;
        final GrenadeType type;
        Vec3 vel;
        Vec3 lastPos;
        int fuse;
        int bounces;
        int lastSoundTick = -100;
        int nextFuzeTick = 0;
        boolean soundStarted;
        boolean soundTailPlayed;
        int restTicks;
        int smokeRest;
        Vec3 soundPos;
        int wallSoundEndTick;
        int tailPendingAt;
        double travel;

        Grenade(Display.ItemDisplay display, ServerLevel level, Player thrower, Vec3 vel, int fuse, GrenadeType type) {
            this.display = display;
            this.level = level;
            this.thrower = thrower;
            this.type = type;
            this.vel = vel;
            this.lastPos = display == null ? Vec3.ZERO : display.position();
            this.fuse = fuse;
        }
    }

    private record PendingMask(String player, String color, int expireTick, int fadeTicks, int startAlpha, double strength) {
    }

    private record PendingThrow(int endTick, int slot, String util) {
    }

    private static final class SmokeZone {
        final ServerLevel level;
        final Vec3 pos;
        final int startTick;
        final int lifeTicks;
        final double maxRadius;
        int nextPuff;
        int volumeId;
        Display.ItemDisplay display;
        int nextCore;
        int nextHiss;
        boolean fadedOut;
        /** 每个玩家最近一次听到放烟声的 tick，用来进出烟区时续播。 */
        final java.util.HashMap<java.util.UUID, Integer> hissTicks = new java.util.HashMap<>();

        SmokeZone(ServerLevel level, Vec3 pos, int startTick, int lifeTicks, double maxRadius, int nextPuff, int volumeId, Display.ItemDisplay display) {
            this.level = level;
            this.pos = pos;
            this.startTick = startTick;
            this.lifeTicks = lifeTicks;
            this.maxRadius = maxRadius;
            this.nextPuff = nextPuff;
            this.volumeId = volumeId;
            this.display = display;
        }
    }

    private static final class SmokeCard {
        final ServerLevel level;
        final String tag;
        final Vec3 from;
        final Vec3 to;
        final float scale;
        final int startTick;
        final int growTicks;

        SmokeCard(ServerLevel level, String tag, Vec3 from, Vec3 to, float scale, int startTick, int growTicks) {
            this.level = level;
            this.tag = tag;
            this.from = from;
            this.to = to;
            this.scale = scale;
            this.startTick = startTick;
            this.growTicks = growTicks;
        }
    }

    private record FireNode(Vec3 pos, float radius) {
    }

    private static final class FireZone {
        final ServerLevel level;
        final Vec3 center;
        final int startTick;
        final int lifeTicks;
        final Player thrower;
        int lastDamageTick;
        int lastPlaceTick;
        final List<FireNode> nodes = new ArrayList<FireNode>();
        final Map<UUID, Integer> burnSteps = new HashMap<UUID, Integer>();

        FireZone(ServerLevel level, Vec3 center, int startTick, int lifeTicks, Player thrower) {
            this.level = level;
            this.center = center;
            this.startTick = startTick;
            this.lifeTicks = lifeTicks;
            this.thrower = thrower;
            this.lastPlaceTick = startTick - 20;
        }
    }

    /** 单个受害者的火区叠层：stage = 已经吃了几跳，nextHitTick = 下一跳的 tick。 */
    private static final class FireVictim {
        Player thrower;
        int stage;
        int nextHitTick;
        int lastSeenTick;

        FireVictim(int now) {
            this.nextHitTick = now + FIRE_HIT_INTERVAL;
            this.lastSeenTick = now;
        }
    }
}

