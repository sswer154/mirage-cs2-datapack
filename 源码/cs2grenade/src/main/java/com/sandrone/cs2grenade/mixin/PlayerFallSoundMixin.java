package com.sandrone.cs2grenade.mixin;

import com.sandrone.cs2grenade.MatchFootsteps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
abstract class PlayerFallSoundMixin {
    @Inject(method = "playBlockFallSound", at = @At("HEAD"), cancellable = true)
    private void cs2$suppressFallSound(CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayer player && MatchFootsteps.suppressVanilla(player)) {
            ci.cancel();
        }
    }
}
