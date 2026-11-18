package com.sandrone.cs2grenade;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * 记录燃烧弹铺下的火方块，重启后仍能从存档里找回并清理。
 */
public final class FireBlockTracker extends SavedData {
    private static final String DATA_NAME = "cs2_fire_blocks";
    private static final String KEY = "positions";

    private final Set<Long> positions = new HashSet<>();

    public static FireBlockTracker get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(factory(), DATA_NAME);
    }

    public static SavedData.Factory<FireBlockTracker> factory() {
        return new SavedData.Factory<>(FireBlockTracker::new, FireBlockTracker::load, DataFixTypes.LEVEL);
    }

    private static FireBlockTracker load(CompoundTag tag, HolderLookup.Provider registries) {
        FireBlockTracker tracker = new FireBlockTracker();
        for (long packed : tag.getLongArray(KEY)) {
            tracker.positions.add(packed);
        }
        return tracker;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putLongArray(KEY, positions.stream().mapToLong(Long::longValue).toArray());
        return tag;
    }

    public void add(BlockPos pos) {
        if (positions.add(pos.asLong())) {
            setDirty();
        }
    }

    public void remove(BlockPos pos) {
        if (positions.remove(pos.asLong())) {
            setDirty();
        }
    }

    public Set<Long> snapshot() {
        return new HashSet<>(positions);
    }
}
