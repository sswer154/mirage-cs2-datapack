package com.sandrone.cs2grenade;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;

/** 数据包用计分板 cs2_sfx / #c4_sfx 触发 C4 音效。 */
final class C4Sfx {
    private static final String COUNTDOWN_SOUND = "c4.countdown";
    private final Set<UUID> countdownListeners = new HashSet<>();
    private Marker countdownBomb;

    void tick(MinecraftServer server, boolean bombRound, boolean paused) {
        if (countdownBomb != null && (!bombRound || paused || countdownBomb.isRemoved()
                || !countdownBomb.getTags().contains("cs2_bomb"))) {
            stopCountdown(server);
        }
        countdownListeners.removeIf(id -> {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player == null) {
                return true;
            }
            if (Cs2Grenade.isMatchAudioListener(player)) {
                return false;
            }
            GrenadeManager.stopSoundFor(player, COUNTDOWN_SOUND);
            return true;
        });

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
        if (paused) {
            return;
        }

        String id;
        float volume;
        switch (code) {
            case 1 -> { id = "c4.planted"; volume = 1.0f; }
            case 2 -> { id = "c4.defused"; volume = 1.0f; }
            case 3 -> { id = null; volume = 0.0f; }
            case 4 -> { id = "c4.plant_start"; volume = 1.0f; }
            case 5 -> { id = "c4.defuse_start"; volume = 1.0f; }
            case 6 -> { id = "c4.explosion"; volume = 4.0f; }
            case 7 -> { id = "c4.beep"; volume = 2.0f; }
            default -> { return; }
        }
        if (code == 2 || code == 6) {
            stopCountdown(server);
        }
        if (id != null) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (Cs2Grenade.isMatchAudioListener(player)) {
                    GrenadeManager.playC4Sfx(player, player.position(), id, volume);
                }
            }
        }
        if ((code == 1 || code == 3) && bombRound) {
            Marker bomb = findBomb(server);
            if (bomb != null && bomb != countdownBomb) {
                stopCountdown(server);
                countdownBomb = bomb;
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    if (Cs2Grenade.isMatchAudioListener(player)) {
                        GrenadeManager.playC4Sfx(player, bomb.position(), COUNTDOWN_SOUND, 2.0f);
                        countdownListeners.add(player.getUUID());
                    }
                }
            }
        }
        Cs2Grenade.debug("C4 音效 code={} 已发送", code);
    }

    void playerLoggedOut(ServerPlayer player) {
        countdownListeners.remove(player.getUUID());
    }

    void stopCountdown(MinecraftServer server) {
        for (UUID id : countdownListeners) {
            ServerPlayer player = server.getPlayerList().getPlayer(id);
            if (player != null) {
                GrenadeManager.stopSoundFor(player, COUNTDOWN_SOUND);
            }
        }
        countdownListeners.clear();
        countdownBomb = null;
    }

    private static Marker findBomb(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            for (Entity entity : level.getEntities().getAll()) {
                if (entity instanceof Marker marker && marker.getTags().contains("cs2_bomb")) {
                    return marker;
                }
            }
        }
        return null;
    }
}
