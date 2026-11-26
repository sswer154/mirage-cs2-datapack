package com.sandrone.cs2grenade.mixin;

import com.sandrone.cs2grenade.MatchFootsteps;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
abstract class PlayerStepSoundMixin {
    @Inject(method = "playStepSound", at = @At("HEAD"), cancellable = true)
    private void cs2$suppressStep(BlockPos pos, BlockState state, CallbackInfo ci) {
        if ((Object) this instanceof ServerPlayer player && MatchFootsteps.suppressVanilla(player)) {
            ci.cancel();
        }
    }
}
