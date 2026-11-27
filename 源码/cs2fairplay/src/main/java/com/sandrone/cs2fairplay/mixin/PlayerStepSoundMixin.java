package com.sandrone.cs2fairplay.mixin;

import com.sandrone.cs2fairplay.FootstepWire;
import com.sandrone.cs2fairplay.ServerFootsteps;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
abstract class PlayerStepSoundMixin {
    @Inject(method = "walkingStepSound", at = @At("HEAD"), cancellable = true)
    private void cs2fairplay$useMatchStepSound(BlockPos pos, BlockState state, CallbackInfo callback) {
        if ((Object) this instanceof ServerPlayer player && ServerFootsteps.customSource(player)) {
            callback.cancel();
        } else if ((Object) this instanceof Player player && player.level().isClientSide()
                && FootstepWire.permits(player.level().dimension().location(), player.getId())) {
            callback.cancel();
        }
    }
}
