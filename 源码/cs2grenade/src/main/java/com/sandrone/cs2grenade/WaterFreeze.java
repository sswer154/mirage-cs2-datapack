package com.sandrone.cs2grenade;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;

final class WaterFreeze {
    private final Map<UUID, Anchor> anchors = new HashMap<>();
    private int lastRound;

    void tick(MinecraftServer server) {
        var board = server.getScoreboard();
        int round = score(board, ScoreHolder.forNameOnly("#epoch"), "cs2_health_round");
        if (round != lastRound) {
            anchors.clear();
            lastRound = round;
        }
        int solo = score(board, ScoreHolder.forNameOnly("#solo"), "cs2_counts");
        boolean trainingHold = solo >= 1 && solo <= 2
                && score(board, ScoreHolder.forNameOnly("#train_hold"), "cs2_counts") == 1;
        if (trainingHold || score(board, ScoreHolder.forNameOnly("#state"), "cs2_state") != 2) {
            anchors.clear();
            return;
        }

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!player.isAlive() || player.isSpectator() || score(board, player, "cs2_inmatch") != 1) {
                remove(player);
                continue;
            }
            var anchor = anchors.get(player.getUUID());
            if (anchor == null || anchor.level() != player.serverLevel()) {
                remove(player);
                if (player.isInWater()) {
                    anchors.put(player.getUUID(), new Anchor(player.serverLevel(), player.position()));
                }
                continue;
            }
            var position = anchor.position();
            if (player.position().distanceToSqr(position) > 0.0004) {
                player.teleportTo(position.x, position.y, position.z);
                player.setDeltaMovement(Vec3.ZERO);
            }
        }
    }

    void remove(ServerPlayer player) {
        anchors.remove(player.getUUID());
    }

    private static int score(Scoreboard board, ScoreHolder holder, String name) {
        var objective = board.getObjective(name);
        var score = objective == null ? null : board.getPlayerScoreInfo(holder, objective);
        return score == null ? 0 : score.value();
    }

    private record Anchor(ServerLevel level, Vec3 position) {}
}
