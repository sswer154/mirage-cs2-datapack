package com.sandrone.cs2fairplay.mixin;

import com.sandrone.cs2fairplay.FairPlayClient;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyMapping.class)
abstract class KeyMappingMixin {
    @Shadow private int clickCount;

    @Unique private boolean cs2fairplay$allowed;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void cs2fairplay$identifyOwner(CallbackInfo callback) {
        cs2fairplay$allowed = StackWalker.getInstance().walk(frames -> frames
                .map(StackWalker.StackFrame::getClassName)
                .filter(name -> !name.equals(KeyMapping.class.getName()))
                .findFirst()
                .map(name -> name.startsWith("net.minecraft.")
                        || name.startsWith("com.tacz.guns.")
                        || name.startsWith("com.sandrone."))
                .orElse(false));
    }

    @Inject(method = "isDown", at = @At("HEAD"), cancellable = true)
    private void cs2fairplay$hideHeldKey(CallbackInfoReturnable<Boolean> callback) {
        if (FairPlayClient.locked() && !cs2fairplay$allowed) callback.setReturnValue(false);
    }

    @Inject(method = "consumeClick", at = @At("HEAD"), cancellable = true)
    private void cs2fairplay$discardClick(CallbackInfoReturnable<Boolean> callback) {
        if (FairPlayClient.locked() && !cs2fairplay$allowed) {
            clickCount = 0;
            callback.setReturnValue(false);
        }
    }
}
