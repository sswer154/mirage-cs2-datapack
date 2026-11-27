package com.sandrone.cs2fairplay;

import net.neoforged.fml.ModList;
import net.neoforged.neoforge.attachment.AttachmentType;

final class SpectatorYsmArms {
    private static final String YSM = "yes_steve_model";
    private static final String SUPPORTED_VERSION = "2.6.5-neoforge+mc1.21.1";
    private static final AttachmentType<Object> ARM_MODEL = findAttachment();

    private SpectatorYsmArms() {}

    static AttachmentType<Object> attachment() {
        return ARM_MODEL;
    }

    // YSM's NeoForge arm event requires a LocalPlayer. Its model state belongs to the
    // watched player, so expose that attachment on our render-only player.
    @SuppressWarnings("unchecked")
    private static AttachmentType<Object> findAttachment() {
        var mod = ModList.get().getModContainerById(YSM);
        if (mod.isEmpty() || !SUPPORTED_VERSION.equals(mod.orElseThrow().getModInfo().getVersion().toString())) return null;
        try {
            var modClass = Class.forName("com.elfmcys.yesstevemodel.YesSteveModel");
            if (!Boolean.TRUE.equals(modClass.getMethod("isAvailable").invoke(null))) return null;
            return (AttachmentType<Object>) Class.forName("com.elfmcys.yesstevemodel.OoooO0OO0000O00oo0Oo00OO")
                    .getField("oOo0OO0O0o000OO0O000oo0o").get(null);
        } catch (ReflectiveOperationException | LinkageError exception) {
            com.mojang.logging.LogUtils.getLogger().warn("Could not initialize YSM spectator arms", exception);
            return null;
        }
    }
}
