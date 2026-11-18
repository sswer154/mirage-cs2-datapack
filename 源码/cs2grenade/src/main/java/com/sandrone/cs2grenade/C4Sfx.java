package com.sandrone.cs2grenade;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.phys.Vec3;

/**
 * 数据包用计分板 cs2_sfx / #c4_sfx 触发 C4 广播音。
 * 1=安放成功 2=拆除成功 3=倒计时。
 */
public final class C4Sfx {
    private C4Sfx() {
    }

    public static void tick(MinecraftServer server) {
        Scoreboard sb = server.getScoreboard();
        var obj = sb.getObjective("cs2_sfx");
        if (obj == null) {
            return;
        }
        var score = sb.getOrCreatePlayerScore(ScoreHolder.forNameOnly("#c4_sfx"), obj);
        int code = score.get();
        if (code == 0) {
            return;
        }
        score.set(0);
        Vec3 pos = bombPos(server);
        String id;
        float volume;
        switch (code) {
            case 1 -> { id = "c4.planted"; volume = 1.0f; }
            case 2 -> { id = "c4.defused"; volume = 1.0f; }
            case 3 -> { id = "c4.countdown"; volume = 2.0f; }
            case 4 -> { id = "c4.plant_start"; volume = 1.0f; }
            case 5 -> { id = "c4.defuse_start"; volume = 1.0f; }
            case 6 -> { id = "c4.explosion"; volume = 4.0f; }
            case 7 -> { id = "c4.beep"; volume = 2.0f; }
            default -> { return; }
        }
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            if (!shouldHear(p)) {
                continue;
            }
            // 广播音在听者自己的位置播放，保证全场都能听到；倒计时在炸弹位置，按距离衰减
            Vec3 at = code == 3 && pos != null ? pos : p.position();
            GrenadeManager.playC4Sfx(p, at, id, volume);
            // 安放成功的同时起倒计时（同一 tick 只能读一个信号，所以并在这里）
            if (code == 1 && pos != null) {
                GrenadeManager.playC4Sfx(p, pos, "c4.countdown", 2.0f);
            }
            // 拆除或引爆时掐掉倒计时
            if (code == 2 || code == 6) {
                GrenadeManager.stopC4CountdownAll();
            }
        }
        Cs2Grenade.debug("C4 音效 code={} 已发送", code);
    }

    private static boolean shouldHear(ServerPlayer player) {
        Scoreboard sb = player.getServer().getScoreboard();
        var inMatch = sb.getObjective("cs2_inmatch");
        if (inMatch != null
                && sb.getOrCreatePlayerScore(ScoreHolder.forNameOnly(player.getScoreboardName()), inMatch).get() == 1) {
            return true;
        }
        return player.getTeam() != null && "cs2_cast".equals(player.getTeam().getName());
    }

    private static Vec3 bombPos(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity e : level.getEntities().getAll()) {
                if (e.getTags().contains("cs2_bomb")) {
                    return e.position();
                }
            }
        }
        return null;
    }
}
