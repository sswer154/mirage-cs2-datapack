package com.sandrone.cs2footsteps.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.sandrone.cs2footsteps.ClientFootsteps;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "eu.ha3.presencefootsteps.sound.player.ImmediateSoundPlayer", remap = false)
abstract class PresenceFootstepsMixin {
    @Inject(method = "playSound", at = @At("HEAD"), cancellable = true, require = 0)
    private void cs2footsteps$skipReplacedFootstep(CallbackInfo callback, @Local(argsOnly = true) LivingEntity source) {
        if (source instanceof Player player && ClientFootsteps.replacesFootsteps(player)) {
            callback.cancel();
        }
    }
}
