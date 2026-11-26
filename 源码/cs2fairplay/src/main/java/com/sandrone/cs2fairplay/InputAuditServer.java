package com.sandrone.cs2fairplay;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class InputAuditServer {
    private static final int FINISHED_LIMIT = 8;
    private static final Map<UUID, Match> FINISHED = new LinkedHashMap<>();
    private static boolean uploadEnabled;
    private static Match active;

    private static final class Match {
        final UUID id = UUID.randomUUID();
        final Path path;
        final String name;
        boolean upload;
        final int start;
        int lastRound = -1;
        final Map<UUID, PlayerLog> players = new HashMap<>();

        Match(MinecraftServer server) throws IOException {
            Path directory = server.getWorldPath(LevelResource.ROOT).resolve("cs2fairplay-audit");
            Files.createDirectories(directory);
            var time = LocalDateTime.now();
            String day = time.format(DateTimeFormatter.BASIC_ISO_DATE);
            String minute = time.format(DateTimeFormatter.ofPattern("HHmm", Locale.ROOT));
            int sequence;
            try (var matches = Files.list(directory)) {
                sequence = matches.map(p -> p.getFileName().toString())
                        .filter(n -> n.startsWith(day + "-") && AuditWire.validName(n))
                        .mapToInt(n -> Integer.parseInt(n.substring(n.lastIndexOf("场次") + 2))).max().orElse(0) + 1;
            }
            name = day + "-" + minute + "-场次" + String.format(Locale.ROOT, "%03d", sequence);
            path = Files.createDirectory(directory.resolve(name));
            upload = uploadEnabled;
            start = server.getTickCount();
            Files.createDirectories(path);
        }

        int tick(MinecraftServer server) { return server.getTickCount() - start; }
    }

    private static final class PlayerLog {
        final String playerId;

        PlayerLog(ServerPlayer player) {
            playerId = AuditCompare.filePlayerId(player.getGameProfile().getName());
        }

        AuditJournal journal;
        int slot = -1;
        ByteArrayOutputStream partial;
        int total;
        boolean comparing;
        boolean complete;
        ServerPlayer connection;
    }

    private InputAuditServer() {}

    static void commands(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("audit")
                .then(Commands.literal("on").executes(c -> set(c.getSource(), true)))
                .then(Commands.literal("off").executes(c -> set(c.getSource(), false)))
                .then(Commands.literal("status").executes(c -> status(c.getSource()))));
    }

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        var node = Commands.literal("cs2audit_lifecycle").requires(s -> s.hasPermission(2))
                .then(Commands.literal("begin").executes(c -> {
                    begin(c.getSource().getServer());
                    return 1;
                })).then(Commands.literal("finish").executes(c -> {
                    finish(c.getSource().getServer());
                    return 1;
                }));
        event.getDispatcher().register(node);
    }

    private static int set(CommandSourceStack source, boolean on) {
        uploadEnabled = on;
        if (active != null && active.upload != on) {
            active.upload = on;
            var message = Component.literal("本场比赛已" + (on ? "开启" : "关闭") + "记录");
            for (var player : source.getServer().getPlayerList().getPlayers()) {
                if (MatchFairPlay.participant(player)) player.sendSystemMessage(message);
            }
            sync(source.getServer());
        }
        return status(source);
    }

    private static int status(CommandSourceStack source) {
        String current = active == null ? "当前无正式赛" : "当前比赛客户端记录和上传" + (active.upload ? "开启" : "关闭");
        source.sendSuccess(() -> Component.literal(current + "，下一场客户端记录和上传" + (uploadEnabled ? "开启" : "关闭")
                + "。服务端始终记录正式赛操作。"), false);
        return uploadEnabled ? 1 : 0;
    }

    private static void begin(MinecraftServer server) {
        if (active != null || !MatchFairPlay.formal(server)
                || server.getPlayerList().getPlayers().stream().noneMatch(MatchFairPlay::participant)) return;
        try {
            active = new Match(server);
            sync(server);
        } catch (IOException e) {
            LogUtils.getLogger().error("Could not create CS2 input audit", e);
        }
    }

    @SubscribeEvent
    public static void tick(ServerTickEvent.Post event) {
        var server = event.getServer();
        if (active == null) {
            if (MatchFairPlay.formal(server)) begin(server);
            if (active == null) return;
        }
        if (!MatchFairPlay.formal(server)) {
            finish(server);
            return;
        }
        int tick = active.tick(server);
        if (tick % 40 == 0 || active.lastRound != round(server)) sync(server);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!MatchFairPlay.participant(player)) continue;
            var recorded = active.players.get(player.getUUID());
            if (recorded == null || recorded.connection != player) attach(server, player);
            recorded = active.players.get(player.getUUID());
            if (recorded == null || recorded.journal == null || player.isSpectator()) continue;
            if (tick % 5 == 0) {
                recorded.journal.write(tick, round(server), "move", sample(player));
            }
            int slot = player.getInventory().selected;
            if (recorded.slot != slot) {
                recorded.slot = slot;
                recorded.journal.write(tick, round(server), "slot", Integer.toString(slot));
            }
            if (tick % 40 == 0) recorded.journal.flush();
        }
        for (PlayerLog log : active.players.values()) {
            if (log.connection != null && !MatchFairPlay.participant(log.connection)) {
                closePlayer(active, log);
            }
        }
    }

    static String sample(net.minecraft.world.entity.player.Player player) {
        return player.getX() + "," + player.getY() + "," + player.getZ() + "," + player.getYRot() + "," + player.getXRot();
    }

    private static int round(MinecraftServer server) {
        return MatchFairPlay.state(server, "#round", "cs2_round");
    }

    private static boolean channel(ServerPlayer player, net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type<?> type) {
        return player.connection != null && NetworkRegistry.hasChannel(player.connection, type.id());
    }

    private static void sync(MinecraftServer server) {
        active.lastRound = round(server);
        for (var player : server.getPlayerList().getPlayers()) {
            if (MatchFairPlay.participant(player)) attach(server, player);
        }
    }

    private static void attach(MinecraftServer server, ServerPlayer player) {
        var match = active;
        var id = player.getUUID();
        var log = match.players.computeIfAbsent(id, key -> new PlayerLog(player));
        if (log.connection != player) {
            if (log.journal != null) log.journal.close();
            log.connection = player;
            try {
                log.journal = new AuditJournal(AuditCompare.file(match.path, id, log.playerId, ".server.tsv"));
                log.journal.write(match.tick(server), round(server), "session", "enter");
            } catch (IOException e) {
                log.journal = null;
                LogUtils.getLogger().warn("Could not open CS2 server input audit", e);
            }
            player.sendSystemMessage(Component.literal("本场比赛已" + (match.upload ? "开启" : "关闭") + "记录"));
        }
        if (channel(player, AuditWire.Clock.TYPE)) {
            PacketDistributor.sendToPlayer(player, new AuditWire.Clock(match.id, match.name, match.tick(server), round(server), match.upload));
        }
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (active != null) {
                var log = active.players.get(player.getUUID());
                if (log != null) closePlayer(active, log);
            }
            for (var match : FINISHED.values()) {
                var log = match.players.get(player.getUUID());
                if (log != null) log.partial = null;
            }
        }
    }

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && channel(player, AuditWire.Finish.TYPE)) {
            for (var match : FINISHED.values()) {
                var log = match.players.get(player.getUUID());
                if (match.upload && log != null) {
                    if (log.complete && channel(player, AuditWire.Ack.TYPE)) {
                        PacketDistributor.sendToPlayer(player, new AuditWire.Finish(match.id, match.name, true));
                        PacketDistributor.sendToPlayer(player, new AuditWire.Ack(match.id));
                    } else if (!log.comparing) {
                        PacketDistributor.sendToPlayer(player, new AuditWire.Finish(match.id, match.name, true));
                    }
                }
            }
        }
    }

    private static void closePlayer(Match match, PlayerLog log) {
        if (log.journal != null) {
            log.journal.close();
            log.journal = null;
        }
        if (log.connection != null && channel(log.connection, AuditWire.Finish.TYPE)) {
            PacketDistributor.sendToPlayer(log.connection, new AuditWire.Finish(match.id, match.name, false));
        }
        log.connection = null;
        log.slot = -1;
    }

    private static void finish(MinecraftServer server) {
        var match = active;
        if (match == null) return;
        active = null;
        if (match.upload) {
            FINISHED.put(match.id, match);
            if (FINISHED.size() > FINISHED_LIMIT) FINISHED.remove(FINISHED.keySet().iterator().next());
        }
        for (var entry : match.players.entrySet()) {
            UUID id = entry.getKey();
            PlayerLog log = entry.getValue();
            if (log.journal != null) log.journal.close();
            log.journal = null;
            log.connection = null;
            writeStatus(match, id, log.playerId, match.upload ? "status=awaiting_client" : "status=upload_disabled");
            var player = server.getPlayerList().getPlayer(id);
            if (player != null && channel(player, AuditWire.Finish.TYPE)) {
                PacketDistributor.sendToPlayer(player, new AuditWire.Finish(match.id, match.name, match.upload));
            }
        }
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) {
        if (active != null) {
            for (var log : active.players.values()) if (log.journal != null) log.journal.close();
        }
        active = null;
        FINISHED.clear();
        uploadEnabled = false;
    }

    public static void action(ServerPlayer player, String kind, String data) {
        if (active == null || !MatchFairPlay.participant(player) || player.isSpectator()) return;
        var log = active.players.get(player.getUUID());
        if (log != null && log.journal != null) {
            log.journal.write(active.tick(player.getServer()), round(player.getServer()), kind, data);
        }
    }

    static void upload(ServerPlayer player, AuditWire.Part part) {
        Match match = FINISHED.get(part.match());
        if (match == null || !match.upload || !channel(player, AuditWire.Ack.TYPE)) return;
        PlayerLog log = match.players.get(player.getUUID());
        if (log == null || log.comparing) return;
        if (log.complete) {
            PacketDistributor.sendToPlayer(player, new AuditWire.Ack(match.id));
            return;
        }
        if (part.total() <= 0 || part.total() > AuditWire.MAX_FILE || part.bytes().length == 0
                || part.offset() < 0 || part.bytes().length > part.total() - part.offset()) return;
        if (part.offset() == 0) {
            log.partial = new ByteArrayOutputStream();
            log.total = part.total();
        }
        if (log.partial == null || log.total != part.total() || log.partial.size() != part.offset()) return;
        log.partial.writeBytes(part.bytes());
        if (log.partial.size() != log.total) return;
        byte[] bytes = log.partial.toByteArray();
        log.partial = null;
        log.comparing = true;
        UUID id = player.getUUID();
        MinecraftServer server = player.getServer();
        CompletableFuture.supplyAsync(() -> AuditCompare.save(match.path, id, log.playerId, bytes)).thenAccept(ok -> server.execute(() -> {
            log.comparing = false;
            log.complete = ok;
            ServerPlayer connected = server.getPlayerList().getPlayer(id);
            if (ok && connected != null && channel(connected, AuditWire.Ack.TYPE)) {
                PacketDistributor.sendToPlayer(connected, new AuditWire.Ack(match.id));
            }
        }));
    }

    private static void writeStatus(Match match, UUID id, String playerId, String status) {
        try {
            Files.writeString(AuditCompare.file(match.path, id, playerId, ".review.txt"), status + "\n");
        } catch (IOException e) {
            LogUtils.getLogger().warn("Could not write CS2 audit status for {}", id, e);
        }
    }
}
