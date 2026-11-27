package com.sandrone.cs2fairplay;

import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.sandrone.cs2fairplay.mixin.SpectatorItemHandMixin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.attachment.AttachmentType;

public final class SpectatorHands {
    private static RenderPlayer proxy;
    private static RenderPlayer rendering;

    private SpectatorHands() {}

    public static AbstractClientPlayer target(Minecraft game) {
        var target = viewTarget(game);
        return target != null && !game.options.hideGui && !target.isSleeping() ? target : null;
    }

    private static AbstractClientPlayer viewTarget(Minecraft game) {
        if (!FairPlayClient.watchingTeam() || game.player == null || !game.player.isSpectator()
                || !game.options.getCameraType().isFirstPerson()) return null;
        return game.getCameraEntity() instanceof AbstractClientPlayer target && target != game.player
                && target.isAlliedTo(game.player) && target.isAlive() && !target.isSpectator()
                ? target : null;
    }

    public static ObservedShot shotSound(Entity shooter, ResourceLocation playedSound) {
        return viewTarget(Minecraft.getInstance()) == shooter && shooter instanceof AbstractClientPlayer target
                ? SpectatorGunRenderer.shotSound(target.getMainHandItem(), playedSound) : null;
    }

    public record ObservedShot(ResourceLocation sound, float volume) {}

    public static void render(Minecraft game, AbstractClientPlayer target, PoseStack pose,
                              MultiBufferSource.BufferSource buffers, float partialTick) {
        if (proxy == null || proxy.target != target || proxy.connection != game.getConnection()) {
            proxy = new RenderPlayer(game, target);
        }
        proxy.copyRenderState(partialTick);
        int light = game.getEntityRenderDispatcher().getPackedLightCoords(target, partialTick);
        rendering = proxy;
        try {
            var hands = (SpectatorItemHandMixin) game.gameRenderer.itemInHandRenderer;
            ItemStack main = target.getMainHandItem();
            if (!SpectatorGunRenderer.render(target, main, pose, light, partialTick)) {
                if (main.isEmpty()) {
                    if (!target.isInvisible()) {
                        float swing = target.swingingArm == InteractionHand.MAIN_HAND ? target.getAttackAnim(partialTick) : 0;
                        pose.pushPose();
                        try {
                            hands.cs2fairplay$renderPlayerArm(pose, buffers, light, 0, swing, target.getMainArm());
                        } finally {
                            pose.popPose();
                        }
                    }
                } else {
                    drawItem(hands, target, InteractionHand.MAIN_HAND, main, partialTick, pose, buffers, light);
                }
                ItemStack off = target.getOffhandItem();
                if (!off.isEmpty()) {
                    drawItem(hands, target, InteractionHand.OFF_HAND, off, partialTick, pose, buffers, light);
                }
            }
            buffers.endBatch();
        } finally {
            rendering = null;
        }
    }

    private static void drawItem(SpectatorItemHandMixin renderer, AbstractClientPlayer target, InteractionHand hand,
                                 ItemStack stack, float partialTick, PoseStack pose,
                                 MultiBufferSource buffers, int light) {
        float swing = target.swingingArm == hand ? target.getAttackAnim(partialTick) : 0;
        renderer.cs2fairplay$renderArmWithItem(target, partialTick, target.getViewXRot(partialTick), hand,
                swing, stack, 0, pose, buffers, light);
    }

    public static LocalPlayer armPlayer(LocalPlayer original) {
        return rendering == null ? original : rendering;
    }

    public static boolean renderGunArm(HumanoidArm arm, PoseStack pose, int light) {
        if (rendering == null) return false;
        var game = Minecraft.getInstance();
        var renderer = (PlayerRenderer) game.getEntityRenderDispatcher().getRenderer(rendering);
        var buffers = game.renderBuffers().bufferSource();
        if (arm == HumanoidArm.RIGHT) {
            renderer.renderRightHand(pose, buffers, light, rendering);
        } else {
            renderer.renderLeftHand(pose, buffers, light, rendering);
        }
        buffers.endBatch();
        return true;
    }

    static void reset() {
        proxy = null;
        SpectatorGunRenderer.reset();
    }

    // This player is used only for render calls. It is never ticked, added to the level,
    // used as the camera or assigned to Minecraft.player.
    private static final class RenderPlayer extends LocalPlayer {
        private final AbstractClientPlayer target;

        RenderPlayer(Minecraft game, AbstractClientPlayer target) {
            super(game, target.clientLevel, game.player.connection, game.player.getStats(),
                    game.player.getRecipeBook(), false, false);
            this.target = target;
            setUUID(target.getUUID());
        }

        void copyRenderState(float partialTick) {
            setPos(target.position());
            setYRot(target.getViewYRot(partialTick));
            setXRot(target.getViewXRot(partialTick));
            setPose(target.getPose());
            getInventory().setItem(0, target.getMainHandItem());
            getInventory().setItem(Inventory.SLOT_OFFHAND, target.getOffhandItem());
            AttachmentType<Object> attachment = SpectatorYsmArms.attachment();
            if (attachment != null) {
                Object state = target.getExistingDataOrNull(attachment);
                if (state != null) {
                    if (getExistingDataOrNull(attachment) != state) setData(attachment, state);
                } else if (hasData(attachment)) {
                    removeData(attachment);
                }
            }
        }

        @Override public PlayerSkin getSkin() { return target.getSkin(); }
        @Override public GameProfile getGameProfile() { return target == null ? super.getGameProfile() : target.getGameProfile(); }
        @Override public HumanoidArm getMainArm() { return target == null ? super.getMainArm() : target.getMainArm(); }
        @Override public boolean isModelPartShown(PlayerModelPart part) { return target.isModelPartShown(part); }
    }
}
