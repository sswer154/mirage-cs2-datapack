package com.sandrone.cs2fairplay.mixin;

import com.sandrone.cs2fairplay.FairPlayClient;
import com.sandrone.cs2fairplay.TaCZKeyBindings;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
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

    @Inject(method = "<init>*", at = @At("RETURN"))
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

    @Unique
    private boolean cs2fairplay$shouldBlock() {
        if (!FairPlayClient.locked()) return false;
        var binding = (KeyMapping) (Object) this;
        var options = Minecraft.getInstance().options;
        if (options != null && binding == options.keySwapOffhand) return true;

        // Preserve the actual game bindings even when their constructor was called through another mod.
        if (cs2fairplay$allowed || TaCZKeyBindings.isAimOrFire(binding)) return false;
        return options == null
                || (binding != options.keyAttack && binding != options.keyUse && binding != options.keyInventory);
    }

    @Inject(method = "isDown", at = @At("HEAD"), cancellable = true)
    private void cs2fairplay$hideHeldKey(CallbackInfoReturnable<Boolean> callback) {
        if (cs2fairplay$shouldBlock()) callback.setReturnValue(false);
    }

    @Inject(method = "consumeClick", at = @At("HEAD"), cancellable = true)
    private void cs2fairplay$discardClick(CallbackInfoReturnable<Boolean> callback) {
        if (cs2fairplay$shouldBlock()) {
            clickCount = 0;
            callback.setReturnValue(false);
        }
    }
}
