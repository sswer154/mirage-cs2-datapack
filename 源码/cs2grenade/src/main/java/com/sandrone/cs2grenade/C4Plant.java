package com.sandrone.cs2grenade;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * C4 安放读条：右键点一次开始，站定读完进度后调用数据包的安放判定。
 * 不再依赖食物组件，因此没有进食动画。
 */
public final class C4Plant {
    private static final Map<UUID, Channel> CHANNELS = new HashMap<>();

    private static final class Channel {
        final ServerPlayer player;
        final Vec3 start;
        final int totalTicks;
        final BossEvent bar;
        int ticks;

        Channel(ServerPlayer player, int totalTicks) {
            this.player = player;
            this.start = player.position();
            this.totalTicks = Math.max(10, totalTicks);
            this.bar = new net.minecraft.server.level.ServerBossEvent(
                    Component.literal("安放 C4"),
                    BossEvent.BossBarColor.RED,
                    BossEvent.BossBarOverlay.PROGRESS);
            this.bar.setProgress(0.0f);
            ((net.minecraft.server.level.ServerBossEvent) this.bar).addPlayer(player);
        }
    }

    private C4Plant() {
    }

    public static boolean isCarryingC4(Player player) {
        return C4Objective.isBomb(player.getMainHandItem());
    }

    public static boolean isChanneling(Player player) {
        return CHANNELS.containsKey(player.getUUID());
    }

    /** 右键点一次：开始或取消读条。 */
    public static void toggle(ServerPlayer player, double seconds) {
        if (CHANNELS.containsKey(player.getUUID())) {
            // 已在读条时再点右键不重播、也不打断，避免每点一次响一声
            return;
        }
        if (!isCarryingC4(player)) {
            return;
        }
        // 只有正式阶段、T 阵营、存活、且是携带者才能开始
        if (score(player, "cs2_inmatch") != 1 || score(player, "cs2_team") != 1
                || score(player, "cs2_alive") != 1 || score(player, "cs2_carrier") != 1) {
            return;
        }
        if (fake("#state", "cs2_state", player) != 3) {
            return;
        }
        if (!nearBombSite(player, 10.0)) {
            return;
        }
        int ticks = (int) Math.round(Math.max(0.6, seconds) * 20.0);
        Channel ch = new Channel(player, ticks);
        CHANNELS.put(player.getUUID(), ch);
        GrenadeManager.playPlantSound((ServerLevel) player.level(), player.position());
        Cs2Grenade.debug("开始安放 C4：{} tick", ticks);
    }

    public static void cancel(Player player, boolean playCancel) {
        Channel ch = CHANNELS.remove(player.getUUID());
        if (ch == null) {
            return;
        }
        ((net.minecraft.server.level.ServerBossEvent) ch.bar).removePlayer((ServerPlayer) player);
        GrenadeManager.stopPlantSound((ServerLevel) player.level(), ch.start);
        if (playCancel) {
            GrenadeManager.playPlantSound((ServerLevel) player.level(), player.position());
        }
    }

    public static void tick(ServerPlayer player) {
        Channel ch = CHANNELS.get(player.getUUID());
        if (ch == null) {
            return;
        }
        // 失效条件：死了、手里没 C4、走开、离开包点
        boolean invalid = !player.isAlive()
                || fake("#state", "cs2_state", player) != 3
                || score(player, "cs2_inmatch") != 1 || score(player, "cs2_alive") != 1
                || score(player, "cs2_carrier") != 1
                || !isCarryingC4(player)
                || player.position().distanceToSqr(ch.start) > 0.8 * 0.8
                || !nearBombSite(player, 10.0);
        if (invalid) {
            cancel(player, false);
            return;
        }
        ch.ticks++;
        float progress = Math.min(1.0f, (float) ch.ticks / (float) ch.totalTicks);
        ((net.minecraft.server.level.ServerBossEvent) ch.bar).setProgress(progress);
        if (ch.ticks >= ch.totalTicks) {
            CHANNELS.remove(player.getUUID());
            ((net.minecraft.server.level.ServerBossEvent) ch.bar).removePlayer(player);
            player.getServer().getCommands().performPrefixedCommand(
                    player.getServer().createCommandSourceStack().withSuppressedOutput(),
                    "execute as " + player.getScoreboardName() + " run function cs2:bomb/plant_check");
            Cs2Grenade.debug("安放读条完成");
        }
    }

    public static void clearAll() {
        for (Channel ch : CHANNELS.values()) {
            ((net.minecraft.server.level.ServerBossEvent) ch.bar).removePlayer(ch.player);
        }
        CHANNELS.clear();
    }

    private static int score(ServerPlayer player, String objective) {
        var sb = player.getServer().getScoreboard();
        var obj = sb.getObjective(objective);
        if (obj == null) {
            return 0;
        }
        return sb.getOrCreatePlayerScore(
                net.minecraft.world.scores.ScoreHolder.forNameOnly(player.getScoreboardName()), obj).get();
    }

    private static int fake(String name, String objective, ServerPlayer player) {
        var sb = player.getServer().getScoreboard();
        var obj = sb.getObjective(objective);
        if (obj == null) {
            return 0;
        }
        return sb.getOrCreatePlayerScore(
                net.minecraft.world.scores.ScoreHolder.forNameOnly(name), obj).get();
    }

    private static boolean nearBombSite(ServerPlayer player, double range) {
        ServerLevel level = (ServerLevel) player.level();
        AABB box = player.getBoundingBox().inflate(range);
        return !level.getEntitiesOfClass(Marker.class, box, marker ->
                player.distanceToSqr(marker) <= range * range
                        && (marker.getTags().contains("cs2_site_a") || marker.getTags().contains("cs2_site_b"))).isEmpty();
    }
}
