package com.sandrone.cs2fairplay;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Records settlement awards for participants who are offline when a round ends. */
final class RoundRewards {
    private static final ScoreHolder SWAP = ScoreHolder.forNameOnly("#swap");

    private RoundRewards() {
    }

    static void register() {
        NeoForge.EVENT_BUS.addListener(RoundRewards::registerCommands);
    }

    private static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("cs2econ")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("team")
                        .then(Commands.argument("side", IntegerArgumentType.integer(1, 2))
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                        .executes(context -> distribute(context.getSource().getServer(),
                                                IntegerArgumentType.getInteger(context, "side"),
                                                IntegerArgumentType.getInteger(context, "amount"), false)))
                                .then(Commands.literal("from")
                                        .then(Commands.argument("holder", StringArgumentType.string())
                                                .executes(context -> {
                                                    var server = context.getSource().getServer();
                                                    var holder = ScoreHolder.forNameOnly(
                                                            StringArgumentType.getString(context, "holder"));
                                                    return distribute(server, IntegerArgumentType.getInteger(context, "side"),
                                                            score(server.getScoreboard(), holder, "cs2_econ"), false);
                                                })))))
                .then(Commands.literal("planter").executes(context ->
                        distribute(context.getSource().getServer(), 1, 300, true)))
                .then(Commands.literal("claim").executes(context ->
                        claim(context.getSource().getPlayerOrException()))));
    }

    private static int distribute(MinecraftServer server, int side, int amount, boolean planterOnly) {
        Scoreboard board = server.getScoreboard();
        Objective participants = board.getObjective("cs2_inmatch");
        Objective money = board.getObjective("cs2_money");
        Objective pending = board.getObjective("cs2_pending_pay");
        if (participants == null || money == null || pending == null || amount <= 0) {
            return 0;
        }
        int swaps = score(board, SWAP, "cs2_side_epoch");
        int recipients = 0;
        for (var entry : board.listPlayerScores(participants)) {
            if (entry.value() != 1) {
                continue;
            }
            ScoreHolder holder = ScoreHolder.forNameOnly(entry.owner());
            int team = score(board, holder, "cs2_team");
            if (team < 1 || team > 2) {
                continue;
            }
            int seenSwaps = score(board, holder, "cs2_side_epoch");
            if (swaps > seenSwaps && ((swaps - seenSwaps) & 1) != 0) {
                team = 3 - team;
            }
            if (team != side || (planterOnly && score(board, holder, "cs2_planter") != 1)) {
                continue;
            }
            boolean online = server.getPlayerList().getPlayerByName(entry.owner()) != null;
            board.getOrCreatePlayerScore(holder, online ? money : pending).add(amount);
            recipients++;
        }
        return recipients;
    }

    private static int claim(ServerPlayer player) {
        Scoreboard board = player.getScoreboard();
        ScoreHolder holder = ScoreHolder.forNameOnly(player.getScoreboardName());
        Objective money = board.getObjective("cs2_money");
        Objective pending = board.getObjective("cs2_pending_pay");
        if (money == null || pending == null || score(board, holder, "cs2_inmatch") != 1) {
            return 0;
        }
        var accrued = board.getPlayerScoreInfo(holder, pending);
        if (accrued == null || accrued.value() <= 0) {
            return 0;
        }
        int amount = accrued.value();
        board.getOrCreatePlayerScore(holder, money).add(amount);
        RoundCheckpoint.creditBaseline(player, amount);
        board.resetSinglePlayerScore(holder, pending);
        return amount;
    }

    private static int score(Scoreboard board, ScoreHolder holder, String name) {
        Objective objective = board.getObjective(name);
        var info = objective == null ? null : board.getPlayerScoreInfo(holder, objective);
        return info == null ? 0 : info.value();
    }
}
