package com.sandrone.cs2fairplay;

import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;

import java.lang.reflect.Method;

final class TaCZAuditEvents {
    private TaCZAuditEvents() {}

    static void register() {
        if (!ModList.get().isLoaded("tacz")) return;
        subscribe("GunFireEvent", "getShooter", "fire", false);
        subscribe("GunReloadEvent", "getEntity", "reload", false);
        subscribe("GunDrawEvent", "getEntity", "draw", false);
        subscribe("EntityHurtByGunEvent$Post", "getAttacker", "hit", true);
    }

    private static void subscribe(String eventName, String actorName, String action, boolean hit) {
        try {
            var type = Class.forName("com.tacz.guns.api.event.common." + eventName).asSubclass(Event.class);
            var actor = type.getMethod(actorName);
            var side = type.getMethod("getLogicalSide");
            var amount = hit ? type.getMethod("getAmount") : null;
            NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, type,
                    event -> record(event, actor, side, amount, action));
        } catch (ReflectiveOperationException | LinkageError e) {
            LogUtils.getLogger().warn("CS2 audit did not load TaCZ event {}", eventName, e);
        }
    }

    private static void record(Event event, Method actor, Method side, Method amount, String action) {
        try {
            if (!(actor.invoke(event) instanceof LivingEntity shooter)) return;
            if (side.invoke(event) == LogicalSide.SERVER && shooter instanceof ServerPlayer player) {
                InputAuditServer.action(player, action, amount == null ? "1" : String.valueOf(amount.invoke(event)));
            } else if (side.invoke(event) == LogicalSide.CLIENT && amount == null) {
                InputAuditClient.weapon(shooter, action);
            }
        } catch (ReflectiveOperationException e) {
            LogUtils.getLogger().warn("Could not record TaCZ event {}", action, e);
        }
    }
}
