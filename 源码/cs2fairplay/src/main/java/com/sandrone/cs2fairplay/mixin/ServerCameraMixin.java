package com.sandrone.cs2fairplay.mixin;

import com.sandrone.cs2fairplay.MatchFairPlay;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
abstract class ServerCameraMixin {
    @Inject(method = "setCamera", at = @At("HEAD"), cancellable = true)
    private void cs2fairplay$validateCamera(Entity target, CallbackInfo callback) {
        if (!MatchFairPlay.allowCamera((ServerPlayer) (Object) this, target)) {
            callback.cancel();
        }
    }
}
