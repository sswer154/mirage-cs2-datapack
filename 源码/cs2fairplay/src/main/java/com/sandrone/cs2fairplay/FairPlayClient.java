package com.sandrone.cs2fairplay;

import net.minecraft.client.CameraType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

public final class FairPlayClient {
    private static boolean locked;
    private static boolean watchingTeam;
    private static CameraType previous;

    private FairPlayClient() {}

    static void register(IEventBus modBus) {
        modBus.addListener(FairPlayClient::registerHud);
        NeoForge.EVENT_BUS.register(FairPlayClient.class);
    }

    private static void registerHud(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.CHAT,
                ResourceLocation.fromNamespaceAndPath(Cs2FairPlay.MOD_ID, "spectator"), FairPlayClient::render);
    }

    static void applyPolicy(boolean lock, boolean watchTeam) {
        var options = Minecraft.getInstance().options;
        watchingTeam = watchTeam;
        if (!watchTeam) SpectatorHands.reset();
        if (lock == locked) return;
        locked = lock;
        KeyMapping.releaseAll();
        if (lock) {
            previous = options.getCameraType();
            options.setCameraType(CameraType.FIRST_PERSON);
        } else if (previous != null) {
            options.setCameraType(previous);
            previous = null;
        }
    }

    static boolean watchingTeam() {
        return watchingTeam;
    }

    public static boolean locked() {
        return locked;
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        var mc = Minecraft.getInstance();
        if (locked && !mc.options.getCameraType().isFirstPerson()) {
            mc.options.setCameraType(CameraType.FIRST_PERSON);
        }
    }

    @SubscribeEvent
    public static void mouse(InputEvent.MouseButton.Pre event) {
        var mc = Minecraft.getInstance();
        int button = event.getButton();
        if (!watchingTeam || mc.player == null || !mc.player.isSpectator() || mc.screen != null
                || (button != GLFW.GLFW_MOUSE_BUTTON_LEFT && button != GLFW.GLFW_MOUSE_BUTTON_RIGHT)) return;
        event.setCanceled(true);
        if (event.getAction() == GLFW.GLFW_PRESS && watchedPlayer(mc) != null && mc.getConnection() != null) {
            PacketDistributor.sendToServer(new Cs2FairPlay.CycleTarget(button == GLFW.GLFW_MOUSE_BUTTON_LEFT));
        }
    }

    @SubscribeEvent
    public static void hideModelsIntersectingCamera(RenderPlayerEvent.Pre event) {
        var mc = Minecraft.getInstance();
        if (!watchingTeam || mc.player == null || !mc.player.isSpectator()) return;
        var camera = mc.gameRenderer.getMainCamera();
        if (!camera.isInitialized() || camera.isDetached()) return;

        var player = event.getEntity();
        var interpolation = player.getPosition(event.getPartialTick()).subtract(player.position());
        // Other players can share the camera's position during a switch or in the lobby.
        if (player == camera.getEntity() || player.getBoundingBox().move(interpolation).inflate(0.1)
                .contains(camera.getPosition())) {
            event.setCanceled(true);
        }
    }

    private static void render(GuiGraphics graphics, DeltaTracker tick) {
        var mc = Minecraft.getInstance();
        if (!watchingTeam || mc.player == null || !mc.player.isSpectator() || mc.options.hideGui
                || mc.screen instanceof ChatScreen) return;
        var target = watchedPlayer(mc);
        var title = Component.translatable("hud.cs2fairplay.dead_spectator");
        var detail = target == null ? Component.translatable("hud.cs2fairplay.waiting")
                : target.getDisplayName();
        var controls = Component.translatable("hud.cs2fairplay.controls");
        int width = Math.max(mc.font.width(title), mc.font.width(detail));
        if (target != null) width = Math.max(width, mc.font.width(controls));
        int x = graphics.guiWidth() / 2;
        // The data pack's score uses the vanilla action bar, including NeoForge's extra HUD height.
        int actionBarOffset = Math.max(68, Math.max(mc.gui.leftHeight, mc.gui.rightHeight) + 9);
        int top = graphics.guiHeight() - actionBarOffset + 8;
        int half = (width + 20) / 2;
        graphics.fill(x - half, top, x + half, top + (target == null ? 26 : 37), 0xC0181A20);
        graphics.drawCenteredString(mc.font, title, x, top + 4, 0xFFFFBE64);
        graphics.drawCenteredString(mc.font, detail, x, top + 15, 0xFFFFFFFF);
        if (target != null) graphics.drawCenteredString(mc.font, controls, x, top + 26, 0xFFCACCD2);
    }

    private static Player watchedPlayer(Minecraft mc) {
        return mc.getCameraEntity() instanceof Player player && player != mc.player && player.isAlliedTo(mc.player)
                ? player : null;
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        applyPolicy(false, false);
    }
}
