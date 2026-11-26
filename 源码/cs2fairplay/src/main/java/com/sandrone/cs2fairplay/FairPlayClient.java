package com.sandrone.cs2fairplay;

import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

public final class FairPlayClient {
    private static boolean locked;
    private static boolean watchingTeam;
    private static CameraType previous;

    private FairPlayClient() {}

    static void register() {
        NeoForge.EVENT_BUS.register(FairPlayClient.class);
    }

    static void applyPolicy(boolean lock, boolean watchTeam) {
        var options = Minecraft.getInstance().options;
        watchingTeam = watchTeam;
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
    public static void render(RenderGuiEvent.Post event) {
        var mc = Minecraft.getInstance();
        if (!watchingTeam || mc.player == null || !mc.player.isSpectator() || mc.options.hideGui) return;
        var graphics = event.getGuiGraphics();
        var target = watchedPlayer(mc);
        var title = Component.translatable("hud.cs2fairplay.dead_spectator");
        var detail = target == null ? Component.translatable("hud.cs2fairplay.waiting")
                : target.getDisplayName();
        var controls = Component.translatable("hud.cs2fairplay.controls");
        int width = Math.max(mc.font.width(title), mc.font.width(detail));
        if (target != null) width = Math.max(width, mc.font.width(controls));
        int x = graphics.guiWidth() / 2;
        int y = graphics.guiHeight() - 84;
        int half = (width + 20) / 2;
        graphics.fill(x - half, y - 5, x + half, y + (target == null ? 26 : 39), 0xC0181A20);
        graphics.drawCenteredString(mc.font, title, x, y, 0xFFFFBE64);
        graphics.drawCenteredString(mc.font, detail, x, y + 13, 0xFFFFFFFF);
        if (target != null) graphics.drawCenteredString(mc.font, controls, x, y + 26, 0xFFCACCD2);
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
