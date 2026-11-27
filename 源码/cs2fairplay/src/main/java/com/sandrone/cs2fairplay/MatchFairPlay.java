package com.sandrone.cs2fairplay;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.scores.ScoreHolder;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Comparator;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MatchFairPlay {
    private static final ScoreHolder STATE = ScoreHolder.forNameOnly("#state");
    private static final ScoreHolder SOLO = ScoreHolder.forNameOnly("#solo");
    private static final String REQUIREMENT = "正式赛需要兼容的 CS2 Fair Play 模组，并禁用小地图及雷达模组。";
    private static final Set<ServerPlayer> WAITING = Collections.newSetFromMap(new IdentityHashMap<>());
    private static final Map<ServerPlayer, Cs2FairPlay.Policy> SENT = new IdentityHashMap<>();

    private MatchFairPlay() {}

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        var root = Commands.literal("cs2fairplay").requires(source -> source.hasPermission(2))
                .then(Commands.literal("ready").executes(context -> require(context.getSource().getPlayerOrException())))
                .then(Commands.literal("check_start").executes(context -> checkStart(context.getSource())))
        .then(Commands.literal("footsteps_all")
                .then(Commands.literal("on").executes(context -> setFootsteps(context, true))
                        .then(Commands.argument("players", EntityArgument.players())
                                .executes(context -> setFootsteps(context, true))))
                .then(Commands.literal("off").executes(context -> setFootsteps(context, false))
                        .then(Commands.argument("players", EntityArgument.players())
                                .executes(context -> setFootsteps(context, false)))))
                .then(Commands.literal("check_formal").executes(context -> checkFormal(context.getSource())))
                .then(Commands.literal("join_t").executes(context -> checkJoin(context.getSource(), "cs2_t")))
                .then(Commands.literal("join_ct").executes(context -> checkJoin(context.getSource(), "cs2_ct")))
                .then(Commands.literal("watch").executes(context ->
                        watch(context.getSource().getPlayerOrException(), 0) ? 1 : 0));
        InputAuditServer.commands(root);
        event.getDispatcher().register(root);
    }

    private static int setFootsteps(CommandContext<CommandSourceStack> context, boolean on)
            throws CommandSyntaxException {
        java.util.Collection<ServerPlayer> targets;
        try {
            targets = EntityArgument.getPlayers(context, "players");
        } catch (IllegalArgumentException e) {
            ServerPlayer self = context.getSource().getPlayer();
            if (self == null) return 0;
            targets = List.of(self);
        }
        for (ServerPlayer target : targets) ServerFootsteps.allow(target, on);
        String names = targets.stream().map(ServerPlayer::getScoreboardName).reduce((a, b) -> a + ", " + b).orElse("");
        context.getSource().sendSuccess(() -> Component.literal(
                (on ? "已开启 " : "已关闭 ") + names + " 的塔科夫脚步"), true);
        return targets.size();
    }

    private static int checkStart(CommandSourceStack source) {
        var blocked = source.getServer().getPlayerList().getPlayers().stream()
                .filter(MatchFairPlay::onTeam)
                .filter(player -> !Cs2FairPlay.ready(player))
                .toList();
        for (var player : blocked) {
            String reason;
            if (!Cs2FairPlay.installed(player)) {
                reason = "未安装 cs2fairplay";
            } else if (!Cs2FairPlay.versionMatches(player)) {
                reason = "cs2fairplay 版本与服务器不一致（需要 " + Cs2FairPlay.version() + "）";
            } else {
                reason = "未通过 cs2fairplay 公平校验，请禁用受限地图模组";
            }
            var message = Component.literal(player.getGameProfile().getName() + " " + reason)
                    .withStyle(ChatFormatting.RED);
            if (source.getEntity() instanceof ServerPlayer starter) {
                starter.sendSystemMessage(message);
            } else {
                source.getServer().sendSystemMessage(message);
            }
            if (player != source.getEntity()) {
                player.displayClientMessage(Component.literal(reason), true);
            }
        }
        return blocked.isEmpty() ? 1 : 0;
    }

    private static int checkFormal(CommandSourceStack source) {
        return checkPlayers(source, source.getServer().getPlayerList().getPlayers().stream()
                .filter(MatchFairPlay::participant).toList());
    }

    private static int checkJoin(CommandSourceStack source, String side) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        MinecraftServer server = source.getServer();
        if (formal(server)) return require(player);
        if (!active(server) || server.getPlayerList().getPlayers().stream()
                .noneMatch(p -> p != player && participant(p) && !side.equals(team(p)))) return 1;
        if (require(player) == 0) return 0;
        return checkFormal(source);
    }

    private static int checkPlayers(CommandSourceStack source, List<ServerPlayer> players) {
        var missing = players.stream().filter(p -> !Cs2FairPlay.ready(p)).toList();
        if (missing.isEmpty()) return 1;
        // The caller may retry the check every tick from the automatic training transition.
        if (!source.isSilent() || source.getServer().getTickCount() % 40 == 0) {
            for (var player : missing) {
                player.displayClientMessage(Component.literal(REQUIREMENT), true);
            }
        }
        if (!source.isSilent()) {
            String names = String.join("、", missing.stream().map(ServerPlayer::getScoreboardName).toList());
            source.sendFailure(Component.literal("未满足正式赛要求：" + names));
        }
        return 0;
    }

    private static int require(ServerPlayer player) {
        if (Cs2FairPlay.ready(player)) return 1;
        player.sendSystemMessage(Component.literal(REQUIREMENT));
        return 0;
    }

    @SubscribeEvent
    public static void tick(ServerTickEvent.Post event) {
        var playerList = event.getServer().getPlayerList();
        WAITING.removeIf(player -> playerList.getPlayer(player.getUUID()) != player);
        SENT.keySet().removeIf(player -> playerList.getPlayer(player.getUUID()) != player);
        for (var player : playerList.getPlayers()) {
            boolean restricted = formal(event.getServer()) && participant(player);
            boolean watching = isTeamViewer(player);
            if (watching || queuedViewer(player)) {
                watch(player, 0);
            } else {
                WAITING.remove(player);
            }
            if (Cs2FairPlay.ready(player)) {
                var policy = new Cs2FairPlay.Policy(restricted, watching && player.isSpectator());
                if (!policy.equals(SENT.put(player, policy))) {
                    PacketDistributor.sendToPlayer(player, policy);
                }
            }
        }
    }

    @SubscribeEvent
    public static void startTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer viewer && event.getTarget() instanceof ServerPlayer target
                && viewer.isSpectator() && viewer.getCamera() == target
                && isTeamViewer(viewer) && validTarget(viewer, target)) {
            // Vanilla ignores an early camera packet if the target has not spawned on the client yet.
            viewer.connection.send(new ClientboundSetCameraPacket(target));
        }
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            WAITING.remove(player);
            SENT.remove(player);
        }
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) {
        WAITING.clear();
        SENT.clear();
    }

    static boolean watch(ServerPlayer viewer, int direction) {
        if (!blocksSpectatorTeleport(viewer)) return false;
        var candidates = candidates(viewer);
        if (candidates.isEmpty()) {
            if (WAITING.add(viewer)) {
                viewer.setCamera(null);
                sendToLobby(viewer);
                String reason = queuedViewer(viewer) ? "已在大厅等待下一回合入场。"
                        : "没有存活队友，已返回大厅等待下一回合。";
                viewer.sendSystemMessage(Component.literal(reason));
            }
            return true;
        }
        WAITING.remove(viewer);
        int current = candidates.indexOf(viewer.getCamera());
        if (direction != 0 || current < 0) {
            int index = current < 0 ? 0 : Math.floorMod(current + direction, candidates.size());
            viewer.setCamera(candidates.get(index));
        }
        return true;
    }

    public static boolean allowCamera(ServerPlayer viewer, Entity target) {
        if (!blocksSpectatorTeleport(viewer)) return true;
        if (target == null || target == viewer) return candidates(viewer).isEmpty();
        return target instanceof ServerPlayer player && validTarget(viewer, player);
    }

    public static boolean blocksSpectatorTeleport(ServerPlayer viewer) {
        return viewer.isSpectator() && (isTeamViewer(viewer) || queuedViewer(viewer));
    }

    private static List<ServerPlayer> candidates(ServerPlayer viewer) {
        return viewer.getServer().getPlayerList().getPlayers().stream()
                .filter(p -> validTarget(viewer, p))
                .sorted(Comparator.comparing(ServerPlayer::getUUID)).toList();
    }

    private static boolean validTarget(ServerPlayer viewer, ServerPlayer target) {
        return target != viewer && participant(target) && team(viewer).equals(team(target))
                && target.isAlive() && !target.isSpectator() && score(target.getServer(), target, "cs2_alive") == 1
                && target.getServer().getPlayerList().getPlayer(target.getUUID()) == target;
    }

    private static void sendToLobby(ServerPlayer player) {
        for (ServerLevel level : player.getServer().getAllLevels()) {
            var points = level.getEntities(EntityType.MARKER, p -> p.getTags().contains("cs2_lobby"));
            if (!points.isEmpty()) {
                var point = points.getFirst();
                player.teleportTo(level, point.getX(), point.getY(), point.getZ(), point.getYRot(), point.getXRot());
                return;
            }
        }
    }

    private static boolean isTeamViewer(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        return formal(server) && participant(player)
                && score(server, player, "cs2_alive") == 0;
    }

    private static boolean queuedViewer(ServerPlayer player) {
        int queued = score(player.getServer(), player, "cs2_queued_team");
        return formal(player.getServer()) && !participant(player) && (queued == 1 || queued == 2);
    }

    static boolean participant(ServerPlayer player) {
        return onTeam(player) && score(player.getServer(), player, "cs2_inmatch") == 1;
    }

    private static boolean onTeam(ServerPlayer player) {
        String side = team(player);
        return side.equals("cs2_t") || side.equals("cs2_ct");
    }

    private static String team(ServerPlayer player) {
        return player.getTeam() == null ? "" : player.getTeam().getName();
    }

    static boolean formal(MinecraftServer server) {
        return active(server) && score(server, SOLO, "cs2_counts") == 0;
    }

    private static boolean active(MinecraftServer server) {
        int phase = score(server, STATE, "cs2_state");
        return phase >= 1 && phase <= 6;
    }

    static int score(MinecraftServer server, ScoreHolder holder, String objectiveName) {
        var board = server.getScoreboard();
        var objective = board.getObjective(objectiveName);
        var value = objective == null ? null : board.getPlayerScoreInfo(holder, objective);
        return value == null ? 0 : value.value();
    }

    static int state(MinecraftServer server, String holder, String objective) {
        return score(server, ScoreHolder.forNameOnly(holder), objective);
    }
}
