package com.sandrone.cs2fairplay;

import com.mojang.logging.LogUtils;
import net.minecraft.client.KeyMapping;

public final class TaCZKeyBindings {
    static final KeyMapping RELOAD = find("ReloadKey", "RELOAD_KEY");
    static final KeyMapping FIRE = find("ShootKey", "SHOOT_KEY");
    private static final KeyMapping AIM = find("AimKey", "AIM_KEY");

    private TaCZKeyBindings() {}

    public static boolean isAimOrFire(KeyMapping binding) {
        return binding != null && (binding == AIM || binding == FIRE);
    }

    private static KeyMapping find(String type, String field) {
        try {
            return (KeyMapping) Class.forName("com.tacz.guns.client.input." + type).getField(field).get(null);
        } catch (ClassNotFoundException absent) {
            return null;
        } catch (ReflectiveOperationException | LinkageError e) {
            LogUtils.getLogger().warn("Could not resolve TaCZ key binding {}", field, e);
            return null;
        }
    }
}
