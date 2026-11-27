package com.sandrone.cs2fairplay;

import com.mojang.logging.LogUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

final class AuditCompare {
    private static final int WINDOW = 12;
    private static final int MAX_TICK = 24 * 60 * 60 * 20;

    private record Entry(int tick, int round, String kind, String value) {}

    static String filePlayerId(String name) {
        return name.replaceAll("[^a-zA-Z0-9_]", "_");
    }

    static Path file(Path directory, UUID player, String playerId, String suffix) {
        return directory.resolve(directory.getFileName() + "-" + playerId + "-" + player + suffix);
    }

    static boolean save(Path path, UUID player, String playerId, byte[] bytes) {
        try {
            var client = read(new String(bytes, StandardCharsets.UTF_8), true);
            var serverPath = file(path, player, playerId, ".server.tsv");
            var server = Files.exists(serverPath) ? read(Files.readString(serverPath), false) : List.<Entry>of();
            Files.write(file(path, player, playerId, ".client.tsv"), bytes);
            Files.writeString(file(path, player, playerId, ".review.txt"), summarize(server, client));
            return true;
        } catch (IOException | IllegalArgumentException e) {
            LogUtils.getLogger().warn("Could not receive CS2 match audit for {}", player, e);
            return false;
        }
    }

    private static List<Entry> read(String content, boolean client) {
        var entries = new ArrayList<Entry>();
        for (String line : content.split("\n")) {
            String[] parts = line.split("\t", -1);
            if (parts.length != 4 || parts[3].length() > 128
                    || (client && !validClientValue(parts[2], parts[3]))) {
                throw new IllegalArgumentException("Invalid audit row");
            }
            int tick = Integer.parseInt(parts[0]);
            int round = Integer.parseInt(parts[1]);
            if (tick < 0 || tick > MAX_TICK || round < 0) throw new IllegalArgumentException("Invalid audit time");
            entries.add(new Entry(tick, round, parts[2], parts[3]));
        }
        return entries;
    }

    private static boolean validClientValue(String kind, String value) {
        return switch (kind) {
            case "session" -> value.equals("enter");
            case "limit" -> value.equals("cap");
            case "slot" -> value.matches("[0-8]");
            case "fire", "reload", "draw" -> value.equals("1");
            case "input" -> value.matches("(forward|back|left|right|jump|sneak|sprint|attack|use|tacz_reload|tacz_fire):(down|up)");
            case "move" -> {
                var coordinates = value.split(",", -1);
                yield coordinates.length == 5 && java.util.Arrays.stream(coordinates)
                        .allMatch(c -> Double.isFinite(Double.parseDouble(c)));
            }
            default -> false;
        };
    }

    private static String summarize(List<Entry> server, List<Entry> client) {
        var positions = new HashMap<Long, List<Entry>>();
        var events = new HashMap<String, List<Entry>>();
        for (Entry e : server) {
            if (e.kind.equals("move")) positions.computeIfAbsent(positionKey(e.round, e.tick), unused -> new ArrayList<>()).add(e);
            else events.computeIfAbsent(e.kind, unused -> new ArrayList<>()).add(e);
        }
        int near = 0;
        int drift = 0;
        int noReference = 0;
        for (Entry c : client) {
            if (!c.kind.equals("move")) continue;
            boolean covered = false;
            boolean matched = false;
            for (int t = Math.max(0, c.tick - WINDOW); t <= c.tick + WINDOW; t++) {
                var nearby = positions.get(positionKey(c.round, t));
                if (nearby == null) continue;
                covered = true;
                if (nearby.stream().anyMatch(s -> similar(s.value, c.value))) {
                    matched = true;
                    break;
                }
            }
            if (matched) near++;
            else if (covered) drift++;
            else noReference++;
        }
        var result = new StringBuilder("status=uploaded\nuse=manual_review_only\n")
                .append("server_rows=").append(server.size()).append('\n')
                .append("client_rows=").append(client.size()).append('\n')
                .append("matched_motion=").append(near).append('\n')
                .append("motion_difference=").append(drift).append('\n')
                .append("motion_without_reference=").append(noReference).append('\n')
                .append("recording_limit_seen=")
                .append(java.util.stream.Stream.concat(server.stream(), client.stream()).anyMatch(e -> e.kind.equals("limit"))).append('\n')
                .append("server_last_tick=").append(lastTick(server)).append('\n')
                .append("client_last_tick=").append(lastTick(client)).append('\n');
        for (String kind : List.of("fire", "reload", "draw", "slot")) {
            var reference = new HashMap<Long, Integer>();
            for (Entry e : events.getOrDefault(kind, List.of())) {
                long key = actionKey(e, kind, e.tick);
                reference.merge(key, 1, Integer::sum);
            }
            int matched = 0;
            int clientOnly = 0;
            for (Entry e : client) {
                if (!e.kind.equals(kind)) continue;
                boolean found = false;
                for (int distance = 0; distance <= WINDOW && !found; distance++) {
                    found = consume(reference, actionKey(e, kind, e.tick - distance))
                            || (distance > 0 && consume(reference, actionKey(e, kind, e.tick + distance)));
                }
                if (found) matched++;
                else clientOnly++;
            }
            result.append(kind).append("_matched=").append(matched).append('\n')
                    .append(kind).append("_client_only=").append(clientOnly).append('\n')
                    .append(kind).append("_server_only=").append(reference.values().stream().mapToInt(Integer::intValue).sum()).append('\n');
        }
        result.append("server_fire_requests=").append(events.getOrDefault("fire_request", List.of()).size()).append('\n')
                .append("server_reload_requests=").append(events.getOrDefault("reload_request", List.of()).size()).append('\n')
                .append("server_draw_requests=").append(events.getOrDefault("draw_request", List.of()).size()).append('\n')
                .append("server_hits=").append(events.getOrDefault("hit", List.of()).size()).append('\n');
        return result.toString();
    }

    private static int lastTick(List<Entry> items) {
        return items.stream().mapToInt(Entry::tick).max().orElse(-1);
    }

    private static long positionKey(int round, int tick) {
        return ((long) round << 32) | Integer.toUnsignedLong(tick);
    }

    private static long actionKey(Entry entry, String kind, int tick) {
        // The clock is elapsed match ticks and remains monotonic across round restarts/halftime.
        char slot = kind.equals("slot") && entry.value.length() == 1 ? entry.value.charAt(0) : 0;
        return ((long) tick << 16) | slot;
    }

    private static boolean consume(Map<Long, Integer> counts, long key) {
        Integer count = counts.get(key);
        if (count == null) return false;
        if (count == 1) counts.remove(key);
        else counts.put(key, count - 1);
        return true;
    }

    private static boolean similar(String left, String right) {
        String[] a = left.split(",");
        String[] b = right.split(",");
        if (a.length != 5 || b.length != 5) return false;
        try {
            double xyz = 0;
            for (int i=0;i<3;i++) {
                double delta = Double.parseDouble(a[i]) - Double.parseDouble(b[i]);
                xyz += delta * delta;
            }
            double yaw = Math.IEEEremainder(Double.parseDouble(a[3]) - Double.parseDouble(b[3]), 360);
            double pitch = Double.parseDouble(a[4]) - Double.parseDouble(b[4]);
            return xyz <= 4.0 && Math.abs(yaw) <= 35 && Math.abs(pitch) <= 25;
        } catch (NumberFormatException invalid) {
            return false;
        }
    }
}
