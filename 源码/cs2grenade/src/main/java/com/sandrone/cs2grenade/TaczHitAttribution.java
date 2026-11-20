package com.sandrone.cs2grenade;

import java.lang.reflect.Method;
import java.util.IdentityHashMap;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

final class TaczHitAttribution {
    private static final String API = "com.tacz.guns.api.event.common.";
    private static final Map<ServerPlayer, Hit> PENDING = new IdentityHashMap<>();

    private TaczHitAttribution() {
    }

    static void register(IEventBus bus) {
        if (!ModList.get().isLoaded("tacz")) {
            return;
        }
        try {
            Class<?> event = Class.forName(API + "EntityHurtByGunEvent");
            Class<? extends Event> pre = Class.forName(event.getName() + "$Pre").asSubclass(Event.class);
            Class<? extends Event> post = Class.forName(event.getName() + "$Post").asSubclass(Event.class);
            Class<?> part = Class.forName(API + "GunDamageSourcePart");
            Reader reader = new Reader(event.getMethod("getLogicalSide"), event.getMethod("getHurtEntity"),
                    event.getMethod("getAttacker"), event.getMethod("getBullet"), event.getMethod("getGunId"),
                    event.getMethod("getDamageSource", part),
                    part.getField("NON_ARMOR_PIERCING").get(null), part.getField("ARMOR_PIERCING").get(null));
            // Read the selected sources after other handlers have had a chance to replace them.
            bus.addListener(EventPriority.LOWEST, pre, reader::onPre);
            bus.addListener(post, reader::onPost);
            bus.addListener(TaczHitAttribution::onServerTick);
        } catch (ReflectiveOperationException | LinkageError | ClassCastException e) {
            Cs2Grenade.LOGGER.warn("[cs2] 无法读取 TaCZ 命中事件，保留伤害源枪械识别", e);
        }
    }

    static Hit consume(ServerPlayer victim, DamageSource source) {
        Hit hit = PENDING.remove(victim);
        return hit != null && (hit.normal() == source || hit.armorPiercing() == source) ? hit : null;
    }

    private static void onServerTick(ServerTickEvent.Post event) {
        // A cancelled or interrupted hit may never post its completion event.
        PENDING.clear();
    }

    record Hit(Entity bullet, ServerPlayer attacker, ResourceLocation gunId,
               DamageSource normal, DamageSource armorPiercing) {
    }

    private record Reader(Method side, Method target, Method attacker, Method bullet, Method gunId,
                          Method source, Object normalPart, Object armorPiercingPart) {
        void onPre(Event event) {
            try {
                if (side.invoke(event) != LogicalSide.SERVER || !(target.invoke(event) instanceof ServerPlayer victim)
                        || !(attacker.invoke(event) instanceof ServerPlayer shooter) || !(bullet.invoke(event) instanceof Entity projectile)
                        || !(gunId.invoke(event) instanceof ResourceLocation id)) {
                    return;
                }
                DamageSource normal = (DamageSource) source.invoke(event, normalPart);
                DamageSource armorPiercing = (DamageSource) source.invoke(event, armorPiercingPart);
                if (normal != null || armorPiercing != null) {
                    PENDING.put(victim, new Hit(projectile, shooter, id, normal, armorPiercing));
                }
            } catch (ReflectiveOperationException | ClassCastException e) {
                Cs2Grenade.debug("TaCZ 命中事件读取失败: {}", e.toString());
            }
        }

        void onPost(Event event) {
            try {
                if (side.invoke(event) == LogicalSide.SERVER && target.invoke(event) instanceof ServerPlayer victim) {
                    Hit hit = PENDING.get(victim);
                    if (hit != null && hit.bullet() == bullet.invoke(event)) {
                        PENDING.remove(victim);
                    }
                }
            } catch (ReflectiveOperationException e) {
                Cs2Grenade.debug("TaCZ 命中结束事件读取失败: {}", e.toString());
            }
        }
    }
}
