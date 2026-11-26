package com.sandrone.cs2fairplay;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.GameModeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.CommandEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

public final class MatchAudit {
    private static final List<Action> PENDING = new ArrayList<>();

    private record Action(List<ServerPlayer> audience, String actor, String description, BooleanSupplier happened) {}

    private MatchAudit() {}

    @SubscribeEvent
    public static void command(CommandEvent event) {
        var parse = event.getParseResults();
        CommandSourceStack source = parse.getContext().getSource();
        var server = source.getServer();
        if (!source.hasPermission(2) || source.isSilent() || !MatchFairPlay.formal(server)) return;
        String input = parse.getReader().getString().strip();
        var audience = server.getPlayerList().getPlayers().stream().filter(MatchFairPlay::participant).toList();
        if (audience.isEmpty()) return;
        String actor = source.getTextName();
        if (input.startsWith("gamemode ") || input.startsWith("minecraft:gamemode ")) {
            if (parse.getContext().getCommand() == null) return;
            try {
                var context = parse.getContext().build(input);
                GameType mode = GameModeArgument.getGameMode(context, "gamemode");
                var targets = parse.getContext().getNodes().stream().anyMatch(n -> n.getNode().getName().equals("target"))
                        ? EntityArgument.getPlayers(context, "target") : List.of(source.getPlayerOrException());
                for (var target : targets) {
                    if (MatchFairPlay.participant(target) && target.gameMode.getGameModeForPlayer() != mode) {
                        PENDING.add(new Action(audience, actor, "将 " + target.getScoreboardName()
                                + " 的模式切换为 " + mode.getName(), () -> target.gameMode.getGameModeForPlayer() == mode));
                    }
                }
            } catch (CommandSyntaxException | IllegalArgumentException ignored) {
                // Invalid or unresolvable commands have no successful match operation to report.
            }
            return;
        }

        int phase = MatchFairPlay.state(server, "#state", "cs2_state");
        int paused = MatchFairPlay.state(server, "#paused", "cs2_counts");
        int round = MatchFairPlay.state(server, "#epoch", "cs2_health_round");
        switch (input) {
            case "function cs2:panel/pause_start", "function cs2:panel/match_pause_start",
                    "function cs2:panel/match_pause" -> {
                if (paused == 0) PENDING.add(new Action(audience, actor, "暂停了比赛",
                        () -> MatchFairPlay.state(server, "#paused", "cs2_counts") == 1));
            }
            case "function cs2:panel/resume", "function cs2:panel/match_resume" -> {
                if (paused == 1) PENDING.add(new Action(audience, actor, "恢复了比赛",
                        () -> MatchFairPlay.state(server, "#paused", "cs2_counts") == 0));
            }
            case "function cs2:panel/restart", "function cs2:round/start" ->
                    PENDING.add(new Action(audience, actor, "重开了本回合",
                            () -> MatchFairPlay.state(server, "#epoch", "cs2_health_round") != round));
            case "function cs2:panel/end", "function cs2:match/end", "function cs2:match/stop" ->
                    PENDING.add(new Action(audience, actor, "结束了比赛",
                            () -> MatchFairPlay.state(server, "#state", "cs2_state") != phase
                                    && !MatchFairPlay.formal(server)));
            default -> { }
        }
    }

    @SubscribeEvent
    public static void tick(ServerTickEvent.Post event) {
        for (Action action : PENDING) {
            if (!action.happened().getAsBoolean()) continue;
            var message = Component.literal("[比赛管理] " + action.actor() + " " + action.description());
            for (var player : action.audience()) {
                if (event.getServer().getPlayerList().getPlayer(player.getUUID()) == player) {
                    player.sendSystemMessage(message);
                }
            }
        }
        PENDING.clear();
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) {
        PENDING.clear();
    }
}
