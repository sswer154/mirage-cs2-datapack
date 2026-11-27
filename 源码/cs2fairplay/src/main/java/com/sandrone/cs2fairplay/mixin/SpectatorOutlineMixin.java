package com.sandrone.cs2fairplay.mixin;

import com.sandrone.cs2fairplay.FairPlayClient;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Minecraft.class)
abstract class SpectatorOutlineMixin {
    @Redirect(method = "shouldEntityAppearGlowing", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/KeyMapping;isDown()Z"))
    private boolean cs2fairplay$restrictSpectatorOutlines(KeyMapping key) {
        return !FairPlayClient.watchingTeam() && key.isDown();
    }
}
