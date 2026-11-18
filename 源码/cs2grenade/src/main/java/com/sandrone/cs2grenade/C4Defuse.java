package com.sandrone.cs2grenade;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * C4 拆除读条：右键点一次开始，站定读完进度后判定拆除成功。
 * 时长沿用数据包 cs2_cfg 的 defuse / kit_time / kit，带钳子时缩短。
 */
public final class C4Defuse {
    private static final Map<UUID, Channel> CHANNELS = new HashMap<>();

    private static final class Channel {
        final ServerPlayer player;
        final Vec3 start;
        final int startSlot;
        final int totalTicks;
        final net.minecraft.server.level.ServerBossEvent bar;
        int ticks;

        Channel(ServerPlayer player, int totalTicks) {
            this.player = player;
            this.start = player.position();
            this.startSlot = player.getInventory().selected;
            this.totalTicks = Math.max(10, totalTicks);
            this.bar = new net.minecraft.server.level.ServerBossEvent(
                    Component.literal("拆除 C4"),
                    BossEvent.BossBarColor.BLUE,
                    BossEvent.BossBarOverlay.PROGRESS);
            this.bar.setProgress(0.0f);
            this.bar.addPlayer(player);
        }
    }

    private C4Defuse() {
    }

    public static boolean isChanneling(ServerPlayer player) {
        return CHANNELS.containsKey(player.getUUID());
    }

    public static void toggle(ServerPlayer player) {
        if (CHANNELS.containsKey(player.getUUID())) {
            cancel(player);
            return;
        }
        // 蹲下才能开始，减少误触
        if (!player.isShiftKeyDown()) {
            Cs2Grenade.debug("拆除未开始：没蹲下");
            return;
        }
        if (!canStart(player)) {
            Cs2Grenade.debug("拆除未开始：state={} team={} alive={} near={}",
                    fake("#state", "cs2_state", player), score(player, "cs2_team"),
                    score(player, "cs2_alive"), nearBomb(player, 3.5));
            return;
        }
        int total = cfg(player, "defuse") * 20;
        if (score(player, "cs2_kit") == 1) {
            total -= Math.max(0, cfg(player, "kit_time")) * 20;
        }
        if (total <= 0) {
            total = 20;
        }
        CHANNELS.put(player.getUUID(), new Channel(player, total));
        GrenadeManager.playDefuseSound((ServerLevel) player.level(), player.position());
        Cs2Grenade.debug("开始拆除 C4：{} tick", total);
    }

    public static void cancel(ServerPlayer player) {
        Channel ch = CHANNELS.remove(player.getUUID());
        if (ch != null) {
            ch.bar.removePlayer(player);
        }
    }

    public static void tick(ServerPlayer player) {
        Channel ch = CHANNELS.get(player.getUUID());
        if (ch == null) {
            return;
        }
        boolean sameSlot = player.getInventory().selected == ch.startSlot;
        boolean sneaking = player.isShiftKeyDown();
        boolean alive = player.isAlive();
        boolean can = canStart(player);
        boolean moved = player.position().distanceToSqr(ch.start) > 0.12 * 0.12;
        boolean look = lookingAtBomb(player);
        if (!(alive && can && !moved && look && sameSlot && sneaking)) {
            Cs2Grenade.debug("拆除中断 alive={} can={} moved={} look={} sameSlot={} sneak={}",
                    alive, can, moved, look, sameSlot, sneaking);
            cancel(player);
            return;
        }
        ch.ticks++;
        ch.bar.setProgress(Math.min(1.0f, (float) ch.ticks / (float) ch.totalTicks));
        if (ch.ticks >= ch.totalTicks) {
            CHANNELS.remove(player.getUUID());
            ch.bar.removePlayer(player);
            player.getServer().getCommands().performPrefixedCommand(
                    player.getServer().createCommandSourceStack().withSuppressedOutput(),
                    "execute as " + player.getScoreboardName() + " run function cs2:bomb/defused");
            Cs2Grenade.debug("拆除读条完成");
        }
    }

    public static void clearAll() {
        for (Channel ch : CHANNELS.values()) {
            ch.bar.removePlayer(ch.player);
        }
        CHANNELS.clear();
    }

    public static boolean canStartFor(ServerPlayer player) {
        return canStart(player);
    }

    private static boolean canStart(ServerPlayer player) {
        if (score(player, "cs2_team") != 2 || score(player, "cs2_alive") != 1) {
            return false;
        }
        if (fake("#state", "cs2_state", player) != 4) {
            return false;
        }
        return nearBomb(player, 3.5);
    }

    /** 读条期间视角要对着炸弹：视线与指向炸弹的方向夹角约 25 度内。 */
    private static boolean lookingAtBomb(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        AABB box = player.getBoundingBox().inflate(3.5);
        for (Entity e : level.getEntitiesOfClass(Entity.class, box,
                ent -> ent.getTags().contains("cs2_bomb"))) {
            Vec3 to = e.position().add(0.0, 0.25, 0.0).subtract(player.getEyePosition()).normalize();
            if (player.getViewVector(1.0f).dot(to) > 0.9) {
                return true;
            }
        }
        return false;
    }

    private static boolean nearBomb(ServerPlayer player, double range) {
        ServerLevel level = (ServerLevel) player.level();
        AABB box = player.getBoundingBox().inflate(range);
        for (Entity e : level.getEntitiesOfClass(Entity.class, box,
                ent -> ent.getTags().contains("cs2_bomb"))) {
            return true;
        }
        return false;
    }

    private static int score(ServerPlayer player, String objective) {
        var sb = player.getServer().getScoreboard();
        var obj = sb.getObjective(objective);
        if (obj == null) {
            return 0;
        }
        return sb.getOrCreatePlayerScore(net.minecraft.world.scores.ScoreHolder.forNameOnly(player.getScoreboardName()), obj).get();
    }

    private static int fake(String name, String objective, ServerPlayer player) {
        var sb = player.getServer().getScoreboard();
        var obj = sb.getObjective(objective);
        if (obj == null) {
            return 0;
        }
        return sb.getOrCreatePlayerScore(net.minecraft.world.scores.ScoreHolder.forNameOnly(name), obj).get();
    }

    private static int cfg(ServerPlayer player, String name) {
        return fake(name, "cs2_cfg", player);
    }
}
