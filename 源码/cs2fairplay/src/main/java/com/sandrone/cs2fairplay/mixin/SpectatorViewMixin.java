package com.sandrone.cs2fairplay.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.sandrone.cs2fairplay.SpectatorHands;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderBuffers;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
abstract class SpectatorViewMixin {
    @Shadow @Final private Minecraft minecraft;
    @Shadow @Final private LightTexture lightTexture;
    @Shadow @Final private RenderBuffers renderBuffers;

    @Inject(method = "renderItemInHand", at = @At(value = "INVOKE",
            target = "Lorg/joml/Matrix4fStack;popMatrix()Lorg/joml/Matrix4fStack;", remap = false))
    private void cs2fairplay$drawObservedHands(Camera camera, float partialTick, Matrix4f view,
                                              CallbackInfo info, @Local PoseStack pose) {
        var target = SpectatorHands.target(minecraft);
        if (target == null) return;
        lightTexture.turnOnLightLayer();
        try {
            SpectatorHands.render(minecraft, target, pose, renderBuffers.bufferSource(), partialTick);
        } finally {
            lightTexture.turnOffLightLayer();
        }
    }
}
