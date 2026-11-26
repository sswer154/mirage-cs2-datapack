package com.sandrone.cs2fairplay;

import com.mojang.logging.LogUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class InputAuditClient {
    private static final ArrayDeque<UUID> QUEUE = new ArrayDeque<>();
    private static final Map<UUID, Path> AUTHORIZED = new HashMap<>();
    private static AuditJournal journal;
    private static AuditWire.Clock clock;
    private static int localTick;
    private static int syncTick;
    private static int selected = -1;
    private static Upload sending;
    private static final KeyMapping RELOAD = taczKey("ReloadKey", "RELOAD_KEY");
    private static final KeyMapping FIRE = taczKey("ShootKey", "SHOOT_KEY");

    private static final class Upload {
        final UUID match;
        final byte[] data;
        int offset;

        Upload(UUID match, byte[] data) {
            this.match = match;
            this.data = data;
        }
    }

    private InputAuditClient() {}

    static void clock(AuditWire.Clock next) {
        if (!AuditWire.validName(next.name())) return;
        if (!next.upload()) {
            closeRecording();
        } else if (journal == null || clock == null || !clock.match().equals(next.match())) {
            closeRecording();
            try {
                Path file = path(next.match(), next.name());
                Path legacy = auditDirectory().resolve(next.name() + "-" + next.match() + ".client.tsv");
                if (!Files.exists(file) && Files.isRegularFile(legacy)) Files.move(legacy, file);
                journal = new AuditJournal(file);
                journal.write(next.tick(), next.round(), "session", "enter");
            } catch (IOException e) {
                LogUtils.getLogger().warn("Could not open local CS2 input audit", e);
            }
            selected = -1;
        }
        clock = next;
        syncTick = localTick;
    }

    static void finish(AuditWire.Finish event) {
        if (!AuditWire.validName(event.name())) return;
        if (clock != null && clock.match().equals(event.match())) closeRecording();
        Path file = path(event.match(), event.name());
        if (!Files.isRegularFile(file)) {
            file = auditDirectory().resolve(event.name() + "-" + event.match() + ".client.tsv");
        }
        if (event.upload() && Files.isRegularFile(file) && AUTHORIZED.putIfAbsent(event.match(), file) == null) {
            QUEUE.addLast(event.match());
        }
    }

    static void ack(AuditWire.Ack event) {
        Path file = AUTHORIZED.remove(event.match());
        if (file == null) return;
        QUEUE.remove(event.match());
        if (sending != null && sending.match.equals(event.match())) sending = null;
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            LogUtils.getLogger().warn("Could not remove acknowledged local CS2 input audit", e);
        }
    }

    private static Path path(UUID id, String name) {
        String playerId = AuditCompare.filePlayerId(Minecraft.getInstance().getUser().getName());
        return auditDirectory().resolve(name + "-" + playerId + "-" + id + ".client.tsv");
    }

    private static Path auditDirectory() {
        return FMLPaths.GAMEDIR.get().resolve("cs2fairplay-audit");
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        localTick++;
        var mc = Minecraft.getInstance();
        if (inGame(mc)) {
            if (localTick % 5 == 0) record("move", InputAuditServer.sample(mc.player));
            int slot = mc.player.getInventory().selected;
            if (selected != slot) {
                selected = slot;
                record("slot", Integer.toString(slot));
            }
        }
        if (journal != null && localTick % 40 == 0) journal.flush();
        if (mc.getConnection() != null) sendNext();
    }

    private static void sendNext() {
        if (sending == null && !QUEUE.isEmpty()) {
            UUID id = QUEUE.removeFirst();
            try {
                Path path = AUTHORIZED.get(id);
                if (path == null) return;
                if (Files.size(path) <= AuditWire.MAX_FILE) sending = new Upload(id, Files.readAllBytes(path));
            } catch (IOException e) {
                LogUtils.getLogger().warn("Could not read local CS2 input audit", e);
            }
        }
        if (sending == null) return;
        int end = Math.min(sending.offset + AuditWire.CHUNK, sending.data.length);
        if (end != sending.offset) {
            PacketDistributor.sendToServer(new AuditWire.Part(sending.match, sending.offset, sending.data.length,
                    Arrays.copyOfRange(sending.data, sending.offset, end)));
        }
        sending.offset = end;
        if (end == sending.data.length) sending = null;
    }

    @SubscribeEvent
    public static void keyboard(InputEvent.Key event) {
        var mc = Minecraft.getInstance();
        if (!inGame(mc) || (event.getAction() != GLFW.GLFW_PRESS && event.getAction() != GLFW.GLFW_RELEASE)) return;
        var options = mc.options;
        KeyMapping[] bindings = {options.keyUp, options.keyDown, options.keyLeft, options.keyRight,
                options.keyJump, options.keyShift, options.keySprint, options.keyAttack, options.keyUse};
        String[] actions = {"forward", "back", "left", "right", "jump", "sneak", "sprint", "attack", "use"};
        for (int i = 0; i < bindings.length; i++) {
            if (bindings[i].matches(event.getKey(), event.getScanCode())) {
                record("input", actions[i] + ":" + (event.getAction() == GLFW.GLFW_PRESS ? "down" : "up"));
            }
        }
        String edge = event.getAction() == GLFW.GLFW_PRESS ? ":down" : ":up";
        if (RELOAD != null && RELOAD.matches(event.getKey(), event.getScanCode())) record("input", "tacz_reload" + edge);
        if (FIRE != null && FIRE.matches(event.getKey(), event.getScanCode())) record("input", "tacz_fire" + edge);
    }

    @SubscribeEvent
    public static void mouse(InputEvent.MouseButton.Post event) {
        var mc = Minecraft.getInstance();
        if (!inGame(mc) || (event.getAction() != GLFW.GLFW_PRESS && event.getAction() != GLFW.GLFW_RELEASE)) return;
        String edge = event.getAction() == GLFW.GLFW_PRESS ? ":down" : ":up";
        if (mc.options.keyAttack.matchesMouse(event.getButton())) record("input", "attack" + edge);
        if (mc.options.keyUse.matchesMouse(event.getButton())) record("input", "use" + edge);
        if (RELOAD != null && RELOAD.matchesMouse(event.getButton())) record("input", "tacz_reload" + edge);
        if (FIRE != null && FIRE.matchesMouse(event.getButton())) record("input", "tacz_fire" + edge);
    }

    private static KeyMapping taczKey(String type, String field) {
        if (!ModList.get().isLoaded("tacz")) return null;
        try {
            return (KeyMapping) Class.forName("com.tacz.guns.client.input." + type).getField(field).get(null);
        } catch (ReflectiveOperationException | LinkageError e) {
            LogUtils.getLogger().warn("Could not resolve TaCZ audit binding {}", field, e);
            return null;
        }
    }

    private static boolean inGame(Minecraft mc) {
        return journal != null && clock != null && mc.isWindowActive() && mc.screen == null
                && mc.player != null && !mc.player.isSpectator() && mc.player.isAlive();
    }

    static void weapon(net.minecraft.world.entity.LivingEntity actor, String kind) {
        Minecraft mc = Minecraft.getInstance();
        if (actor instanceof LocalPlayer && actor == mc.player && inGame(mc)) record(kind, "1");
    }

    private static void record(String kind, String data) {
        journal.write(clock.tick() + localTick - syncTick, clock.round(), kind, data);
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        closeRecording();
        QUEUE.clear();
        AUTHORIZED.clear();
        sending = null;
    }

    private static void closeRecording() {
        if (journal != null) journal.close();
        journal = null;
        clock = null;
    }
}
