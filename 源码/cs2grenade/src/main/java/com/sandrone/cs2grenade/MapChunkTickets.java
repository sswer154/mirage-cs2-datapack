package com.sandrone.cs2grenade;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.ColumnPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** Owns map tickets separately from vanilla /forceload and other mods. */
final class MapChunkTickets extends SavedData {
    private static final TicketController CONTROLLER = new TicketController(
            ResourceLocation.fromNamespaceAndPath(Cs2Grenade.MODID, "map_chunks"));
    private static final String DATA_NAME = "cs2_map_chunks";
    private static final Factory<MapChunkTickets> FACTORY = new Factory<>(
            MapChunkTickets::new, MapChunkTickets::load, DataFixTypes.LEVEL);

    private final Set<Long> chunks = new HashSet<>();

    static void register(IEventBus modBus) {
        modBus.addListener(MapChunkTickets::registerTicketController);
        NeoForge.EVENT_BUS.addListener(MapChunkTickets::registerCommands);
    }

    private static void registerTicketController(RegisterTicketControllersEvent event) {
        event.register(CONTROLLER);
    }

    private static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("cs2chunks")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("retain")
                        .then(Commands.argument("position", ColumnPosArgument.columnPos())
                                .executes(context -> retain(context.getSource().getLevel(),
                                        ColumnPosArgument.getColumnPos(context, "position").toChunkPos()))))
                .then(Commands.literal("release_all").executes(context -> releaseAll(context.getSource()))));
    }

    private static int retain(ServerLevel level, ChunkPos chunk) {
        MapChunkTickets tickets = level.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
        CONTROLLER.forceChunk(level, BlockPos.ZERO, chunk.x, chunk.z, true, true);
        if (tickets.chunks.add(chunk.toLong())) {
            tickets.setDirty();
        }
        return 1;
    }

    private static int releaseAll(CommandSourceStack source) {
        int released = 0;
        for (ServerLevel level : source.getServer().getAllLevels()) {
            MapChunkTickets tickets = level.getDataStorage().get(FACTORY, DATA_NAME);
            if (tickets == null || tickets.chunks.isEmpty()) {
                continue;
            }
            for (long packed : tickets.chunks) {
                ChunkPos chunk = new ChunkPos(packed);
                if (CONTROLLER.forceChunk(level, BlockPos.ZERO, chunk.x, chunk.z, false, true)) {
                    released++;
                }
            }
            tickets.chunks.clear();
            tickets.setDirty();
        }
        return released;
    }

    private static MapChunkTickets load(CompoundTag tag, HolderLookup.Provider registries) {
        MapChunkTickets tickets = new MapChunkTickets();
        for (long packed : tag.getLongArray("Chunks")) {
            tickets.chunks.add(packed);
        }
        return tickets;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putLongArray("Chunks", chunks.stream().mapToLong(Long::longValue).toArray());
        return tag;
    }
}
