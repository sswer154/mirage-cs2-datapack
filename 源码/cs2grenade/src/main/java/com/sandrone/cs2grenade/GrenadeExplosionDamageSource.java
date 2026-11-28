package com.sandrone.cs2grenade;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Player;

/** Marks the mod's HE damage while retaining vanilla explosion behavior and tags. */
public final class GrenadeExplosionDamageSource extends DamageSource {
    GrenadeExplosionDamageSource(ServerLevel level, Player thrower) {
        super(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(DamageTypes.EXPLOSION), thrower, null);
    }
}
