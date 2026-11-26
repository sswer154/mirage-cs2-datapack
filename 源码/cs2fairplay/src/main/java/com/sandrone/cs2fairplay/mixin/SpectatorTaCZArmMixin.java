package com.sandrone.cs2fairplay.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sandrone.cs2fairplay.SpectatorHands;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.tacz.guns.util.RenderHelper", remap = false)
abstract class SpectatorTaCZArmMixin {
    @Inject(method = "renderFirstPersonArm", at = @At("HEAD"), cancellable = true, require = 0)
    private static void cs2fairplay$renderObservedGunArm(LocalPlayer player, HumanoidArm arm, PoseStack pose,
                                                       int light, CallbackInfo info) {
        if (SpectatorHands.renderGunArm(arm, pose, light)) info.cancel();
    }
}
