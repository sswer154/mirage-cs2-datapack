package com.sandrone.cs2fairplay;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

final class MatchState {
    // Keep the existing attachment and chunk-ticket identifiers readable after the move.
    static final String SAVED_NAMESPACE = "cs2grenade";

    private MatchState() {}

    static void register(IEventBus modBus) {
        MatchInventory.register(modBus);
        MatchSpawns.register(modBus);
        MapChunkTickets.register(modBus);
        RoundCheckpoint.register(modBus);
        RoundRewards.register();

        var gameBus = NeoForge.EVENT_BUS;
        gameBus.addListener(MatchStats::registerCommands);
        TaczHitAttribution.register(gameBus);
        gameBus.addListener(EventPriority.LOWEST, KillFeedHandler::onLivingDeath);

        var waterFreeze = new WaterFreeze();
        gameBus.addListener((ServerTickEvent.Post event) -> waterFreeze.tick(event.getServer()));
        gameBus.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                waterFreeze.remove(player);
            }
        });
    }
}
