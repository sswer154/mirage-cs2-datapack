package com.sandrone.cs2fairplay.mixin;

import com.sandrone.cs2fairplay.SpectatorHands;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemInHandRenderer.class)
abstract class SpectatorArmPlayerMixin {
    @Redirect(method = "renderPlayerArm", at = @At(value = "FIELD",
            target = "Lnet/minecraft/client/Minecraft;player:Lnet/minecraft/client/player/LocalPlayer;"))
    private LocalPlayer cs2fairplay$renderedArmPlayer(Minecraft game) {
        return SpectatorHands.armPlayer(game.player);
    }
}
