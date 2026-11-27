package com.sandrone.cs2fairplay;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public final class ServerFootsteps {
    private static final Map<ServerPlayer, FootstepWire.Policy> SENT = new IdentityHashMap<>();

    private ServerFootsteps() {}

    public static boolean customSource(ServerPlayer player) {
        return MatchFairPlay.participant(player) && player.isAlive() && !player.isSpectator()
                && MatchFairPlay.score(player.getServer(), player, "cs2_alive") == 1
                && Cs2FairPlay.supportsFootsteps(player);
    }

    @SubscribeEvent
    public static void tick(ServerTickEvent.Post event) {
        var players = event.getServer().getPlayerList();
        SENT.keySet().removeIf(player -> players.getPlayer(player.getUUID()) != player);
        Map<ServerPlayer, List<Integer>> sources = new IdentityHashMap<>();
        for (ServerPlayer player : players.getPlayers()) {
            if (canHear(player) && Cs2FairPlay.supportsFootsteps(player)) sources.put(player, new ArrayList<>());
        }
        for (ServerPlayer source : players.getPlayers()) {
            if (!customSource(source)) continue;
            sources.get(source).add(source.getId());
            for (ServerPlayer listener : source.serverLevel().getChunkSource().chunkMap.getPlayersWatching(source)) {
                var allowed = sources.get(listener);
                if (listener != source && allowed != null && listener.serverLevel() == source.serverLevel()) {
                    allowed.add(source.getId());
                }
            }
        }
        for (ServerPlayer listener : players.getPlayers()) {
            if (!Cs2FairPlay.supportsFootsteps(listener)) continue;
            var allowed = sources.get(listener);
            if (allowed != null) allowed.sort(null);
            var policy = new FootstepWire.Policy(listener.level().dimension().location(),
                    allowed == null ? List.of() : List.copyOf(allowed));
            if (!policy.equals(SENT.put(listener, policy))) PacketDistributor.sendToPlayer(listener, policy);
        }
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) { SENT.clear(); }

    private static boolean canHear(ServerPlayer player) {
        return MatchFairPlay.participant(player)
                || (player.getTeam() != null && "cs2_cast".equals(player.getTeam().getName()));
    }
}
