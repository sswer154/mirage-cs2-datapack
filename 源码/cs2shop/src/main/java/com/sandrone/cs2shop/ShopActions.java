package com.sandrone.cs2shop;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

import java.util.List;

/** 把「条件判断」和「执行指令」这两件事集中在这里。 */
public final class ShopActions {

    private static final String OBJ = "cs2shop_cond";
    private static final String HOLDER = "#cs2shop";

    private ShopActions() {
    }

    /**
     * 条件写成一条原版指令（不带前导斜杠），执行成功视为通过。
     * 例如 `score @s cs2_money >= 2500` 会自动补成 `execute if score ...`。
     * 因为 performPrefixedCommand 不返回值，这里用 store success 把成败写进计分板再读。
     */
    public static boolean testCondition(MinecraftServer server, ServerPlayer player, String condition) {
        if (condition == null || condition.isBlank()) {
            return true;
        }
        String cond = condition.trim();
        if (cond.startsWith("/")) {
            cond = cond.substring(1);
        }
        if (!cond.startsWith("execute ")) {
            cond = "execute " + cond;
        }
        Objective objective = ensureObjective(server);
        if (objective == null) {
            return false;
        }
        run(player, "scoreboard players set " + HOLDER + " " + OBJ + " 0");
        run(player, "execute store success score " + HOLDER + " " + OBJ + " run " + cond);
        return server.getScoreboard()
                .getOrCreatePlayerScore(ScoreHolder.forNameOnly(HOLDER), objective)
                .get() > 0;
    }

    public static void runCommands(ServerPlayer player, List<String> commands) {
        for (String raw : commands) {
            String cmd = raw.trim();
            if (cmd.isEmpty()) {
                continue;
            }
            run(player, cmd);
        }
    }

    private static Objective ensureObjective(MinecraftServer server) {
        Scoreboard board = server.getScoreboard();
        Objective existing = board.getObjective(OBJ);
        if (existing != null) {
            return existing;
        }
        return board.addObjective(OBJ, ObjectiveCriteria.DUMMY,
                Component.literal("cs2shop"), ObjectiveCriteria.RenderType.INTEGER, false, null);
    }

    private static void run(ServerPlayer player, String rawCommand) {
        Cs2Shop.debug("执行指令: {}", rawCommand);
        String cmd = rawCommand.startsWith("/") ? rawCommand.substring(1) : rawCommand;
        CommandSourceStack source = player.createCommandSourceStack().withPermission(2);
        // tellraw 是给玩家看的提示，要放行；其余命令吞掉，免得刷「正在运行函数 …」
        if (cmd.startsWith("tellraw ")) {
            player.server.getCommands().performPrefixedCommand(source, cmd);
        } else {
            player.server.getCommands().performPrefixedCommand(source.withSuppressedOutput(), cmd);
        }
    }
}
