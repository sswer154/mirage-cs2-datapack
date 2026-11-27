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
    @Shadow public abstract String getName();

    @Unique private boolean cs2fairplay$allowed;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void cs2fairplay$identifyOwner(CallbackInfo callback) {
        String keyName = getName();
        cs2fairplay$allowed = StackWalker.getInstance().walk(frames -> frames
                .map(StackWalker.StackFrame::getClassName)
                .filter(name -> !name.equals(KeyMapping.class.getName()))
                .findFirst()
                .map(owner -> cs2fairplay$isAllowedBinding(owner, keyName))
                .orElse(false));
    }

    @Unique
    private static boolean cs2fairplay$isAllowedBinding(String owner, String keyName) {
        return owner.startsWith("net.minecraft.")
                || owner.startsWith("com.tacz.guns.")
                || owner.startsWith("com.sandrone.")
                || (owner.startsWith("xaero.map.") && "gui.xaero_open_map".equals(keyName));
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
