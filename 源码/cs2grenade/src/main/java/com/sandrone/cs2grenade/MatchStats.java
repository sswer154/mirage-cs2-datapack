package com.sandrone.cs2grenade;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.scores.ScoreHolder;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

final class MatchStats {
    private MatchStats() {}

    static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("cs2stats").requires(source -> source.hasPermission(2))
                .then(Commands.literal("announce_top_kills").executes(ctx -> announce(ctx.getSource().getServer()))));
    }

    private static int announce(MinecraftServer server) {
        var board = server.getScoreboard();
        var inMatch = board.getObjective("cs2_inmatch");
        var kills = board.getObjective("cs2_kills");
        if (inMatch == null || kills == null) return 0;

        int best = 0;
        List<String> leaders = new ArrayList<>();
        for (var entry : board.listPlayerScores(inMatch)) {
            if (entry.value() != 1 || entry.owner().startsWith("#")) continue;
            var score = board.getPlayerScoreInfo(ScoreHolder.forNameOnly(entry.owner()), kills);
            if (score == null || score.value() == 0 || score.value() < best) continue;
            if (score.value() > best) {
                best = score.value();
                leaders.clear();
            }
            leaders.add(entry.owner());
        }
        leaders.sort(Comparator.naturalOrder());
        var message = best == 0 ? Component.literal("本场没有产生击杀。").withStyle(ChatFormatting.GRAY)
                : Component.literal("本场击杀王：").withStyle(ChatFormatting.GOLD);
        for (int i = 0; i < leaders.size(); i++) {
            if (i != 0) message.append(Component.literal("、").withStyle(ChatFormatting.GRAY));
            String name = leaders.get(i);
            var player = server.getPlayerList().getPlayerByName(name);
            message.append(player == null ? Component.literal(name).withStyle(ChatFormatting.YELLOW) : player.getDisplayName());
        }
        if (best > 0) message.append(Component.literal("  " + best + " 杀").withStyle(ChatFormatting.GOLD));
        for (var player : server.getPlayerList().getPlayers()) {
            var score = board.getPlayerScoreInfo(player, inMatch);
            if (score != null && score.value() == 1) player.sendSystemMessage(message);
        }
        return leaders.size();
    }
}
