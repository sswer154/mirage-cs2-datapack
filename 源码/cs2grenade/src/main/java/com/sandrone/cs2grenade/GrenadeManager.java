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

import com.mojang.math.Transformation;
import com.sandrone.cs2grenade.Cs2Grenade;
import net.minecraft.util.Brightness;
import org.joml.Vector3f;
import com.sandrone.cs2grenade.GrenadeLoader;
import com.sandrone.cs2grenade.GrenadeType;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.Util;
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
import net.minecraft.world.level.material.FluidState;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreHolder;

public final class GrenadeManager {
    static final ResourceKey<DamageType> FIRE_DAMAGE_TYPE = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("cs2", "fire"));
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
    private static final String SMOKE_HISS_SOUND = "smoke.hiss";
    private static final float SMOKE_HISS_VOLUME = 0.8f;
    private static final double SMOKE_HISS_RANGE = 16.0 * SMOKE_HISS_VOLUME;
    // hiss.ogg 长 8 秒，按实际时间续播，避免同一颗烟的两段音效重叠。
    private static final long SMOKE_HISS_DURATION_MS = 8_000;
    private static final Map<UUID, Map<SmokeZone, Long>> SMOKE_HISS_LISTENERS = new HashMap<>();
    private static int smokeVolumeId;
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

    static void tick(boolean paused) {
        tickRings();
        GrenadeManager.processMasks();
        GrenadeManager.tickSmoke();
        GrenadeManager.tickSmokeHiss(paused, Util.getMillis());
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

    static void clearRoundGrenades(MinecraftServer server) {
        for (Grenade grenade : ACTIVE) {
            if (grenade.display != null) {
                grenade.display.discard();
            }
        }
        ACTIVE.clear();
        PENDING_THROW.clear();
        FIRE_ZONES.forEach(GrenadeManager::extinguish);
        FIRE_ZONES.clear();
        FIRE_VICTIMS.clear();

        for (SmokeZone zone : SMOKE_ZONES) {
            if (zone.display != null) {
                PERSIST_DISPLAYS.remove(zone.display.getUUID());
                zone.display.discard();
            }
        }
        purgeTransientVisuals(server);
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
        stopAllSmokeHiss(server);
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

    /** 地面反弹时切向速度的保留比例（带一点摩擦，避免落地后像冰壶一样滑）。 */
    private static final double BOUNCE_FRICTION_GROUND = 0.82;
    /** 墙面反弹时切向速度的保留比例，墙面基本不吃摩擦，回弹更明显。 */
    private static final double BOUNCE_FRICTION_WALL = 0.96;
    /** 反弹后的竖直速度低于这个值就直接压到 0，避免停在方块上每 tick 抖一下。 */
    private static final double BOUNCE_MIN_VERTICAL = 0.05;
    /** 贴地滑行时每 tick 的水平速度保留比例。 */
    private static final double GROUND_ROLL_FRICTION = 0.78;
    private static final int PROJECTILE_CHUNK_WAIT_TICKS = 100;

    /** 弹体正下方 0.08 格内有没有碰撞面，用来判断是不是贴着地面在滑。 */
    private static boolean hasSupportBelow(ServerLevel level, Vec3 pos) {
        BlockHitResult hit = level.clip(new ClipContext(pos, pos.add(0.0, -0.08, 0.0),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty()));
        return hit.getType() == HitResult.Type.BLOCK && hit.getDirection() == Direction.UP && !hit.isInside();
    }

    private static void waitForProjectileChunk(Grenade grenade) {
        if (++grenade.chunkWaitTicks < PROJECTILE_CHUNK_WAIT_TICKS) return;
        if (grenade.display != null) grenade.display.discard();
        grenade.fuse = 0;
    }

    private static void step(Grenade g) {
        Vec3 from = g.lastPos;
        if (!g.level.isLoaded(BlockPos.containing(from))) {
            waitForProjectileChunk(g);
            return;
        }
        Vec3 previousVel = g.vel;
        ++g.age;
        g.vel = g.vel.add(0.0, -g.type.gravity(), 0.0).scale(g.type.drag());
        Vec3 to = from.add(g.vel);
        if (!g.level.isLoaded(BlockPos.containing(to))) {
            g.vel = previousVel;
            waitForProjectileChunk(g);
            return;
        }
        g.chunkWaitTicks = 0;
        // 燃烧弹把水也当成碰撞面：落水时在水面爆开，而不是一路沉到水底才炸
        ClipContext.Fluid fluidClip = "fire".equals(g.type.effect()) ? ClipContext.Fluid.WATER : ClipContext.Fluid.NONE;
        BlockHitResult hit = g.level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, fluidClip, (Entity)g.thrower));
        if (hit.getType() == HitResult.Type.BLOCK) {
            int nowTick;
            Vec3 normal = Vec3.atLowerCornerOf((Vec3i)hit.getDirection().getNormal());
            double impactSpeed = g.vel.length();
            // 只对法向做反弹、切向按摩擦保留：以前整条速度一起乘 bounce，落地一脚就趴下
            double vn = g.vel.dot(normal);
            Vec3 normalPart = normal.scale(vn);
            Vec3 tangentPart = g.vel.subtract(normalPart);
            boolean floorHit = normal.y > 0.7;
            double restitution = floorHit ? g.type.bounce() : g.type.bounceWall();
            double friction = floorHit ? BOUNCE_FRICTION_GROUND : BOUNCE_FRICTION_WALL;
            Vec3 bounced = tangentPart.scale(friction).subtract(normalPart.scale(restitution));
            // 反弹后竖直速度已经很小就直接压实，避免贴在地面上每 tick 抖一下
            if (floorHit && Math.abs(bounced.y) < BOUNCE_MIN_VERTICAL) {
                bounced = new Vec3(bounced.x, 0.0, bounced.z);
            }
            g.vel = bounced;
            to = hit.getLocation().add(normal.scale(0.02));
            ++g.bounces;
            if ("fire".equals(g.type.effect()) && isWater(g.level, hit.getBlockPos())) {
                // 水面（或从侧面扎进水里）也算引爆面：爆点抬到水柱顶面，火焰就贴着水面烧
                double surfaceY = waterSurfaceY(g.level, hit.getBlockPos());
                Vec3 at = new Vec3(hit.getLocation().x, surfaceY, hit.getLocation().z);
                g.level.sendParticles((ParticleOptions)ParticleTypes.SPLASH, at.x, at.y, at.z, 24, 0.45, 0.1, 0.45, 0.08);
                g.level.sendParticles((ParticleOptions)ParticleTypes.CLOUD, at.x, at.y + 0.2, at.z, 8, 0.35, 0.15, 0.35, 0.02);
                g.fuse = 0;
                GrenadeManager.explode(g, at);
                return;
            }
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
        // 贴地滑行：竖直速度已经被压平、下面又有支撑面时，水平速度每 tick 掉一点
        if (Math.abs(g.vel.y) < BOUNCE_MIN_VERTICAL && hasSupportBelow(g.level, to)) {
            g.vel = new Vec3(g.vel.x * GROUND_ROLL_FRICTION, g.vel.y, g.vel.z * GROUND_ROLL_FRICTION);
        }
        g.lastPos = to;
        if (g.display != null && g.display.isAlive()) {
            g.display.setPos(to.x, to.y + g.type.renderOffsetY(), to.z);
        }
        if ((g.age & 1) == 0) {
            g.level.sendParticles((ParticleOptions)ParticleTypes.SMOKE, to.x, to.y, to.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
        if ("smoke".equals(g.type.effect())) {
            // 烟雾弹没有引信：只有真正贴地停稳、或碰到火才起烟。
            // 下方没有支撑面的一律不算停稳：抛物线顶点、卡方块边缘、被击退的短暂零速都不起烟。
            boolean supported = hasSupportBelow(g.level, to)
                    && Math.abs(g.vel.y) < BOUNCE_MIN_VERTICAL * 3.0
                    && g.vel.horizontalDistanceSqr() < 0.12 * 0.12;
            if (supported) {
                ++g.smokeRest;
            } else {
                g.smokeRest = 0;
            }
            if (to.y < g.level.getMinBuildHeight() - 8) {
                // 掉进虚空的兜底：直接移除，不在虚空里凭空起烟
                if (g.display != null && !g.display.isRemoved()) {
                    g.display.discard();
                }
                g.fuse = 0;
                return;
            }
            net.minecraft.core.BlockPos at = net.minecraft.core.BlockPos.containing(to);
            boolean burning = g.level.getBlockState(at).is(net.minecraft.world.level.block.Blocks.FIRE)
                    || g.level.getBlockState(at).is(net.minecraft.world.level.block.Blocks.LAVA)
                    || inFireZone(g.level, at);
            // 不再递减兜底引信：飞行时间再长也不在空中起烟
            if (g.smokeRest >= 3 || burning) {
                g.fuse = 0;
            }
        } else if (--g.fuse == 20) {
            // empty if block
        }
        if (g.fuse <= 0) {
            GrenadeManager.explode(g, to);
        }
    }

    /** 烟幕从起烟到铺满的时间（tick）：0.6 秒，期间烟片连续外扩。 */
    private static final int SMOKE_SPREAD_TICKS = 12;
    /** 烟片扩散的关键帧间隔（tick），和 teleport_duration 对齐，客户端插值刚好接上。 */
    private static final int SMOKE_KEYFRAME_TICKS = 3;
    /** 起烟瞬间烟区就已覆盖的比例：烟片一开始是一团贴在一起的大烟球，覆盖范围不能从 0 起。 */
    private static final double SMOKE_INITIAL_COVER = 0.45;

    private static void moveSmokeCard(Display.ItemDisplay display, Vec3 pos, float scale, int interpolationTicks) {
        display.setPosRotInterpolationDuration(interpolationTicks);
        display.setTransformationInterpolationDuration(interpolationTicks);
        display.teleportTo(pos.x, pos.y, pos.z);
        scaleSmokeCard(display, scale);
    }

    private static void scaleSmokeCard(Display.ItemDisplay display, float scale) {
        display.setTransformation(new Transformation(null, null, new Vector3f(scale), null));
        display.setTransformationInterpolationDelay(0);
    }

    private static List<SmokeCard> spawnSmokeCards(ServerLevel level, Vec3 pos, double radius, int id) {
        Vec3 center = pos.add(0.0, 0.6, 0.0);
        // 确保烟片生成的区块已加载；远距离投掷时落点区块可能还没到手
        level.getChunkAt(BlockPos.containing(center));
        int[] ringCounts = new int[]{1, 10, 24, 42, 60};
        double[] ringRadii = new double[]{0.0, 0.25, 0.5, 0.75, 1.0};
        float[] ringScaleBoost = new float[]{1.0f, 1.04f, 1.03f, 1.02f, 1.0f};
        float baseScale = 7.6f;
        List<SmokeCard> cards = new ArrayList<>();
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
                var display = new Display.ItemDisplay(EntityType.ITEM_DISPLAY, level);
                ItemStack model = new ItemStack(Items.PAPER);
                model.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(modelCmd));
                display.setItemStack(model);
                display.addTag("cs2_smoke_card");
                display.addTag("cs2_smoke_" + id);
                display.setBrightnessOverride(new Brightness(15, 15));
                display.setBillboardConstraints(Display.BillboardConstraints.CENTER);
                display.teleportTo(center.x, center.y, center.z);
                display.setPosRotInterpolationDuration(SMOKE_KEYFRAME_TICKS);
                display.setTransformationInterpolationDuration(SMOKE_KEYFRAME_TICKS);
                scaleSmokeCard(display, scale * 0.35f);
                if (level.addFreshEntity(display)) {
                    cards.add(new SmokeCard(display.getUUID(), center, new Vec3(x, y, z), scale));
                }
                ++index;
            }
        }
        return cards;
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
        // 落点没有可用支撑面时也要给引爆反馈，但不能在空中留下伤害区。
        placeFire(zone, 4.0);
        zone.lastPlaceTick = startTick;
        if (zone.nodes.isEmpty()) {
            level.sendParticles(ParticleTypes.SMOKE, pos.x, pos.y, pos.z, 8, 0.25, 0.15, 0.25, 0.03);
        } else {
            FIRE_ZONES.add(zone);
        }
        level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.8f, 1.1f);
        if (g.display != null && !g.display.isRemoved()) {
            g.display.discard();
        }
    }

    private static final double FIRE_NODE_RADIUS = 0.8;
    private static final int MAX_FIRE_NODES = 320;
    private static final double FIRE_VERTICAL_TOLERANCE = 0.65;
    private static final int FIRE_NEARBY_RADIUS = 2;
    private static final double FIRE_HEADROOM = 1.8;
    private static final double FIRE_SURFACE_OFFSET = 0.05;
    private static final double FIRE_STEP_HEIGHT = 1.0 + 1.0e-6;

    private static void placeFire(FireZone zone, double radius) {
        zone.nodes.clear();
        BlockPos origin = BlockPos.containing(zone.center);
        Map<BlockPos, Optional<FireSurface>> surfaces = new HashMap<>();
        FireSurface center = findFireCenter(zone.level, zone.center, origin, surfaces);
        if (center == null) {
            center = findFireCenterNearby(zone.level, zone.center, origin, surfaces);
        }
        if (center == null) {
            return;
        }
        double maxY = center.pos().y + FIRE_STEP_HEIGHT;
        PriorityQueue<FireStep> queue = new PriorityQueue<>(Comparator.comparingDouble(FireStep::distance));
        Map<BlockPos, Double> shortest = new HashMap<>();
        shortest.put(center.cell(), 0.0);
        queue.add(new FireStep(center, 0.0));
        while (!queue.isEmpty() && zone.nodes.size() < MAX_FIRE_NODES) {
            FireStep step = queue.remove();
            FireSurface current = step.surface();
            if (step.distance() > shortest.get(current.cell())) continue;
            addFireNodeAt(zone, current.pos());
            for (int dx = -1; dx <= 1; ++dx) {
                for (int dz = -1; dz <= 1; ++dz) {
                    if (dx == 0 && dz == 0) continue;
                    boolean diagonal = dx != 0 && dz != 0;
                    double distance = step.distance() + (diagonal ? Math.sqrt(2.0) : 1.0);
                    if (distance > radius) continue;
                    FireSurface next = findFireGround(zone.level, current, dx, dz, surfaces);
                    if (next == null || next.pos().y > maxY
                            || distance >= shortest.getOrDefault(next.cell(), Double.POSITIVE_INFINITY)) continue;
                    if (!fireSpreadPathAllowed(zone.level, current, next)) continue;
                    if (diagonal && !canSpreadDiagonally(zone.level, current, next, dx, dz, surfaces)) continue;
                    shortest.put(next.cell(), distance);
                    queue.add(new FireStep(next, distance));
                }
            }
        }
    }

    private static boolean canSpreadDiagonally(ServerLevel level, FireSurface from, FireSurface to,
            int dx, int dz, Map<BlockPos, Optional<FireSurface>> cache) {
        // 薄层可留在同一条斜向路径里。真实台阶仍走直向，且两侧都必须能通行。
        if (Math.abs(from.pos().y - to.pos().y) > 0.25) return false;
        FireSurface xSide = findFireGround(level, from, dx, 0, cache);
        FireSurface zSide = findFireGround(level, from, 0, dz, cache);
        double maxSideHeight = Math.max(from.pos().y, to.pos().y) + 0.25;
        return xSide != null && zSide != null
                && xSide.pos().y <= maxSideHeight && zSide.pos().y <= maxSideHeight
                && fireSpreadPathAllowed(level, from, xSide) && fireSpreadPathAllowed(level, xSide, to)
                && fireSpreadPathAllowed(level, from, zSide) && fireSpreadPathAllowed(level, zSide, to);
    }

    private record FireSurface(BlockPos cell, Vec3 pos) {}
    private record FireStep(FireSurface surface, double distance) {}

    private static FireSurface findFireCenter(ServerLevel level, Vec3 impact, BlockPos column,
            Map<BlockPos, Optional<FireSurface>> cache) {
        FireSurface best = null;
        double bestDistance = Double.POSITIVE_INFINITY;
        Vec3 start = impact.add(0.0, 0.1, 0.0);
        for (int dy = -4; dy <= 2; ++dy) {
            FireSurface candidate = fireSurface(level, column.above(dy), cache);
            if (candidate == null) continue;
            double distance = impact.distanceToSqr(candidate.pos());
            if (distance < bestDistance && fireRayClear(level, start, candidate.pos())) {
                best = candidate;
                bestDistance = distance;
            }
        }
        return best;
    }

    private static FireSurface findFireCenterNearby(ServerLevel level, Vec3 impact, BlockPos origin,
            Map<BlockPos, Optional<FireSurface>> cache) {
        FireSurface best = null;
        double bestDistance = Double.POSITIVE_INFINITY;
        for (int dx = -FIRE_NEARBY_RADIUS; dx <= FIRE_NEARBY_RADIUS; ++dx) {
            for (int dz = -FIRE_NEARBY_RADIUS; dz <= FIRE_NEARBY_RADIUS; ++dz) {
                if (dx == 0 && dz == 0) continue;
                FireSurface candidate = findFireCenter(level, impact, origin.offset(dx, 0, dz), cache);
                if (candidate == null) continue;
                double distance = impact.distanceToSqr(candidate.pos());
                if (distance < bestDistance) {
                    best = candidate;
                    bestDistance = distance;
                }
            }
        }
        return best;
    }

    private static FireSurface findFireGround(ServerLevel level, FireSurface from, int dx, int dz,
            Map<BlockPos, Optional<FireSurface>> cache) {
        BlockPos column = BlockPos.containing(from.pos()).offset(dx, 0, dz);
        for (int dy = 2; dy >= -1; --dy) {
            FireSurface next = fireSurface(level, column.above(dy), cache);
            if (next != null && Math.abs(next.pos().y - from.pos().y) <= FIRE_STEP_HEIGHT) return next;
        }
        return null;
    }

    private static FireSurface fireSurface(ServerLevel level, BlockPos cell,
            Map<BlockPos, Optional<FireSurface>> cache) {
        return cache.computeIfAbsent(cell, pos -> Optional.ofNullable(sampleFireSurface(level, pos))).orElse(null);
    }

    private static FireSurface sampleFireSurface(ServerLevel level, BlockPos cell) {
        BlockPos support = cell.below();
        if (!level.isLoaded(support) || isWater(level, cell)) return null;
        double x = support.getX() + 0.5;
        double z = support.getZ() + 0.5;
        double bottom = support.getY();
        VoxelShape shape = level.getBlockState(support).getCollisionShape(level, support);
        // 只认格子中心能支撑的上表面，如关闭的活板门。草、铁轨等由下面的方块支撑。
        BlockHitResult hit = shape.clip(new Vec3(x, bottom + 2.0, z), new Vec3(x, bottom - 0.01, z), support);
        double height = hit == null || hit.getDirection() != Direction.UP ? 0.0 : hit.getLocation().y - bottom;
        FluidState fluid = level.getFluidState(support);
        if (fluid.is(FluidTags.WATER)) {
            height = Math.max(height, fluid.getHeight(level, support));
        }
        if (height <= 0.0) return null;
        Vec3 surface = new Vec3(x, bottom + height + FIRE_SURFACE_OFFSET, z);
        return fireRayClear(level, surface, surface.add(0.0, FIRE_HEADROOM, 0.0)) ? new FireSurface(cell, surface) : null;
    }

    /** 该格是不是水（含水源、流动水和含水方块）。 */
    private static boolean isWater(ServerLevel level, BlockPos pos) {
        FluidState fluid = level.getFluidState(pos);
        return fluid.is(FluidTags.WATER);
    }

    /** 从某格的水面往上找到水柱顶面（最多 32 格），用于把水面当引爆面。 */
    private static double waterSurfaceY(ServerLevel level, BlockPos pos) {
        BlockPos top = pos;
        for (int i = 0; i < 32 && isWater(level, top.above()); ++i) {
            top = top.above();
        }
        return top.getY() + level.getFluidState(top).getHeight(level, top);
    }

    private static void addFireNodeAt(FireZone zone, Vec3 at) {
        if (zone.extinguished.contains(at)) return;
        if (GrenadeManager.smokeCovers(zone.level, at)) {
            zone.extinguished.add(at);
            return;
        }
        zone.nodes.add(new FireNode(at, (float)FIRE_NODE_RADIUS));
    }

    private static boolean fireSpreadPathAllowed(ServerLevel level, FireSurface from, FireSurface to) {
        Vec3 start = from.pos();
        Vec3 end = to.pos();
        if (Math.abs(start.y - end.y) > FIRE_STEP_HEIGHT) return false;
        // 高的一侧为踏步高度，两格中心之间仍须有净空。
        double height = Math.max(start.y, end.y);
        return fireRayClear(level, new Vec3(start.x, height, start.z), new Vec3(end.x, height, end.z));
    }

    private static boolean fireRayClear(ServerLevel level, Vec3 start, Vec3 end) {
        return level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, CollisionContext.empty())).getType() == HitResult.Type.MISS;
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

    private static final int SMOKE_CLEAR_TICKS = 20;
    private static final int SMOKE_RECOVERY_TICKS = 50;
    private static final int SMOKE_RECOVERY_FRAME_TICKS = 10;

    private static void disperseSmoke(ServerLevel level, Vec3 blast, double shockRadius) {
        if (shockRadius <= 0.0) return;
        int now = level.getServer().getTickCount();
        for (SmokeZone zone : SMOKE_ZONES) {
            if (zone.level != level || now - zone.startTick >= zone.lifeTicks) continue;
            double cover = zone.coverRadius(now);
            if (cover <= 0.0) continue;
            double reach = shockRadius + cover;
            if (blast.distanceToSqr(zone.pos) >= reach * reach) continue;

            SmokeHole hole = new SmokeHole(blast, shockRadius, now);
            zone.holes.add(hole);
            double spread = smokeSpreadEase(zone.startTick, now);
            for (SmokeCard card : zone.cards) {
                Vec3 position = card.position(spread);
                if (hole.clears(position, now) && level.getEntity(card.entityId) instanceof Display.ItemDisplay display) {
                    moveSmokeCard(display, position, 0.0f, 0);
                    card.hiddenByBlast = true;
                }
            }
            zone.nextVisualTick = now;
            level.sendParticles(ParticleTypes.CLOUD, blast.x, blast.y, blast.z,
                    12, 0.6, 0.3, 0.6, 0.12);
        }
    }

    private static void tickSmokeVisuals(SmokeZone zone, int now, int remaining) {
        if (now < zone.nextVisualTick) return;
        zone.holes.removeIf(hole -> hole.radiusAt(now) <= 0.0);
        boolean growing = now < zone.startTick + SMOKE_SPREAD_TICKS + 1;
        boolean fading = remaining <= 60;
        int frameTicks = growing ? SMOKE_KEYFRAME_TICKS : SMOKE_RECOVERY_FRAME_TICKS;
        int frameEnd = now + frameTicks;
        zone.nextVisualTick = frameEnd;
        double spread = smokeSpreadEase(zone.startTick, frameEnd);
        float fadeScale = 3.4f * Math.max(0, remaining - frameTicks) / 60.0f;
        for (SmokeCard card : zone.cards) {
            Vec3 position = card.position(spread);
            boolean hidden = zone.isCleared(position, frameEnd);
            if (!growing && !fading && hidden == card.hiddenByBlast) continue;
            float scale = fading ? fadeScale : (float)(card.scale * (0.35 + 0.65 * spread));
            if (zone.level.getEntity(card.entityId) instanceof Display.ItemDisplay display) {
                moveSmokeCard(display, position, hidden ? 0.0f : scale, frameTicks);
                card.hiddenByBlast = hidden;
            }
        }
    }

    private static double smokeSpreadEase(int startTick, int now) {
        double progress = Math.clamp((double)(now - startTick - 1) / SMOKE_SPREAD_TICKS, 0.0, 1.0);
        return 1.0 - Math.pow(1.0 - progress, 2.2);
    }

    /** 有效覆盖由扩散半径和当前仍开着的局部缺口共同决定。 */
    private static boolean smokeCovers(ServerLevel level, Vec3 pos) {
        int now = serverRef == null ? 0 : serverRef.getTickCount();
        for (SmokeZone s : SMOKE_ZONES) {
            if (s.level != level || now - s.startTick >= s.lifeTicks) continue;
            double radius = s.coverRadius(now);
            if (radius <= 0.0 || s.pos.distanceToSqr(pos) > radius * radius || s.isCleared(pos, now)) continue;
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
            fireZone.nodes.removeIf(node -> {
                if (!GrenadeManager.smokeCovers(fireZone.level, node.pos)) return false;
                fireZone.extinguished.add(node.pos);
                return true;
            });
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
            if (hurtFromFire(victim, rec.thrower, fireDamage) && isEnemy(rec.thrower, victim)) {
                GrenadeManager.addDamageScore(rec.thrower, "cs2_fire_dmg", fireDamage);
            }
            ++rec.stage;
            rec.nextHitTick = now + FIRE_HIT_INTERVAL;
        }
        FIRE_VICTIMS.entrySet().removeIf(entry -> now - entry.getValue().lastSeenTick > FIRE_STAY_GRACE);
        FIRE_ZONES.removeAll(dead);
    }

    private static boolean hurtFromFire(LivingEntity victim, Player thrower, float amount) {
        int previousCooldown = victim.invulnerableTime;
        float previousDamage = victim.lastHurt;
        try {
            // 火区有自己的结算间隔，且不能改写普通攻击的受伤冷却。
            victim.invulnerableTime = 0;
            return victim.hurt(victim.level().damageSources().source(FIRE_DAMAGE_TYPE, thrower), amount);
        } finally {
            victim.invulnerableTime = previousCooldown;
            victim.lastHurt = previousDamage;
        }
    }

    private static void smokeExplode(Grenade g, Vec3 pos) {
        ServerLevel level = g.level;
        int life = Math.max(60, g.type.cloudSeconds() * 20);
        int volumeId = ++smokeVolumeId;
        List<SmokeCard> cards = spawnSmokeCards(level, pos, g.type.cloudRadius(), volumeId);
        int born = serverRef == null ? 0 : serverRef.getTickCount();
        SmokeZone zone = new SmokeZone(level, pos, born, life, g.type.cloudRadius(), 0, volumeId, g.display, cards);
        SMOKE_ZONES.add(zone);
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
            int remaining = z.lifeTicks - elapsed;
            if (remaining <= 0) {
                z.level.getServer().getCommands().performPrefixedCommand(z.level.getServer().createCommandSourceStack().withPermission(4).withSuppressedOutput(), "kill @e[tag=cs2_smoke_" + z.volumeId + "]");
                if (z.display != null) {
                    PERSIST_DISPLAYS.remove(z.display.getUUID());
                    z.display.discard();
                }
                expired.add(z);
                continue;
            }
            if (!z.fadedOut && remaining <= 60) {
                z.fadedOut = true;
                z.nextVisualTick = now;
                GrenadeManager.playCs2(z.level, z.pos, "smoke.end", 0.8f, 1.0f);
            }
            tickSmokeVisuals(z, now, remaining);
        }
        SMOKE_ZONES.removeAll(expired);
    }

    private static boolean canHearSmoke(ServerPlayer player, SmokeZone zone) {
        return !zone.fadedOut && player.level() == zone.level
                && player.distanceToSqr(zone.pos) <= SMOKE_HISS_RANGE * SMOKE_HISS_RANGE;
    }

    private static void tickSmokeHiss(boolean paused, long now) {
        if (serverRef == null || (SMOKE_ZONES.isEmpty() && SMOKE_HISS_LISTENERS.isEmpty())) {
            return;
        }
        if (paused) {
            stopAllSmokeHiss(serverRef);
            return;
        }
        for (ServerPlayer player : serverRef.getPlayerList().getPlayers()) {
            UUID id = player.getUUID();
            Map<SmokeZone, Long> sounds = SMOKE_HISS_LISTENERS.get(id);
            if (!Cs2Grenade.isMatchAudioListener(player)) {
                if (sounds != null) {
                    stopSoundFor(player, SMOKE_HISS_SOUND);
                    SMOKE_HISS_LISTENERS.remove(id);
                }
                continue;
            }
            if (sounds != null) {
                sounds.values().removeIf(end -> now >= end);
            }
            for (SmokeZone zone : SMOKE_ZONES) {
                if (!canHearSmoke(player, zone)) {
                    continue;
                }
                if (sounds == null) {
                    sounds = new HashMap<>();
                    SMOKE_HISS_LISTENERS.put(id, sounds);
                }
                if (sounds.putIfAbsent(zone, now + SMOKE_HISS_DURATION_MS) == null) {
                    playCs2For(player, zone.level, zone.pos, SMOKE_HISS_SOUND, SMOKE_HISS_VOLUME, 1.0f);
                }
            }
            // 淡出的烟不再续播，让这一段自然播完，避免停掉其他烟的同名音效。
            if (sounds != null && sounds.isEmpty()) {
                SMOKE_HISS_LISTENERS.remove(id);
            }
        }
    }

    static void clearSmokeListener(ServerPlayer player) {
        SMOKE_HISS_LISTENERS.remove(player.getUUID());
    }

    private static void stopAllSmokeHiss(MinecraftServer server) {
        for (UUID id : SMOKE_HISS_LISTENERS.keySet()) {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player != null) {
                stopSoundFor(player, SMOKE_HISS_SOUND);
            }
        }
        SMOKE_HISS_LISTENERS.clear();
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
                    if (victim.hurt(new GrenadeExplosionDamageSource(level, thrower), fragDamage * factor) && isEnemy(thrower, victim)) {
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
            float ringVolume = (float)(0.35 + 0.85 * intensity);
            if (prevRemaining <= 0 || (ringVolume >= prevRing.volume() && ticks + fadeTicks > prevRemaining)) {
                Cs2Grenade.debug("耳鸣登记 {} 强度={} 保持={}t 淡出={}t",
                        p.getScoreboardName(), String.format(Locale.ROOT, "%.2f", intensity), ticks, fadeTicks);
                stopRingSounds(p);
                RING_STATE.put(p.getUUID(), new Ring(ringNow + ticks, fadeTicks, 0, ringVolume, 0));
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
        if (pierceDamage > 0.0f && ("he".equals(g.type.effect()) || "impact".equals(g.type.effect()))) {
            disperseSmoke(level, pos, pierceOuter);
        }
        AABB pierceBox = new AABB(pos, pos).inflate(pierceOuter);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, pierceBox)) {
            double dist;
            Player p;
            if (e instanceof Player && (p = (Player)e).isSpectator() || (dist = e.position().distanceTo(pos)) > pierceOuter) continue;
            float factor = dist <= pierceInner ? 1.0f : (float)(1.0 - (dist - pierceInner) / (pierceOuter - pierceInner));
            if (e.hurt(new GrenadeExplosionDamageSource(level, g.thrower), pierceDamage * factor) && isEnemy(g.thrower, e)) {
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
            // 四档传播：0-20 / 20-40 / 40-60 / 60-80 格
            if (d > 80.0) {
                continue;
            }
            String layer;
            if (d < 20.0) {
                layer = fire ? "grenade.explosion_fire_close" : "grenade.explosion_close";
            } else if (d < 40.0) {
                layer = "grenade.explosion_far";
            } else if (d < 60.0) {
                layer = "grenade.explosion_distant";
            } else {
                layer = "grenade.explosion_far3";
            }
            float loud = 5.0f;
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
        /** 弹体存活的 tick 数，用于尾迹等周期性效果，不再借用 fuse。 */
        int age;
        int chunkWaitTicks;
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
        int nextVisualTick;
        boolean fadedOut;
        final List<SmokeHole> holes = new ArrayList<>();
        final List<SmokeCard> cards;

        SmokeZone(ServerLevel level, Vec3 pos, int startTick, int lifeTicks, double maxRadius, int nextPuff, int volumeId, Display.ItemDisplay display, List<SmokeCard> cards) {
            this.level = level;
            this.pos = pos;
            this.startTick = startTick;
            this.lifeTicks = lifeTicks;
            this.maxRadius = maxRadius;
            this.nextPuff = nextPuff;
            this.volumeId = volumeId;
            this.cards = cards;
            this.display = display;
            this.nextVisualTick = startTick + 1;
        }

        /** 当前 tick 的有效覆盖半径：跟烟片扩散同步变大，视觉铺满时正好到 maxRadius。 */
        double coverRadius(int now) {
            double ease = smokeSpreadEase(this.startTick, now);
            return this.maxRadius * (SMOKE_INITIAL_COVER + (1.0 - SMOKE_INITIAL_COVER) * ease);
        }

        boolean isCleared(Vec3 point, int now) {
            for (SmokeHole hole : holes) {
                if (hole.clears(point, now)) return true;
            }
            return false;
        }
    }

    private record SmokeHole(Vec3 center, double radius, int startTick) {
        double radiusAt(int now) {
            int recoveryAge = now - startTick - SMOKE_CLEAR_TICKS;
            double recovery = Math.clamp(recoveryAge / (double)SMOKE_RECOVERY_TICKS, 0.0, 1.0);
            return radius * (1.0 - recovery);
        }

        boolean clears(Vec3 point, int now) {
            double currentRadius = radiusAt(now);
            return currentRadius > 0.0 && center.distanceToSqr(point) < currentRadius * currentRadius;
        }
    }

    private static final class SmokeCard {
        final UUID entityId;
        final Vec3 from;
        final Vec3 to;
        final float scale;
        boolean hiddenByBlast;

        SmokeCard(UUID entityId, Vec3 from, Vec3 to, float scale) {
            this.entityId = entityId;
            this.from = from;
            this.to = to;
            this.scale = scale;
        }

        Vec3 position(double spread) {
            return from.add(to.subtract(from).scale(spread));
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
        final Set<Vec3> extinguished = new HashSet<>();
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

