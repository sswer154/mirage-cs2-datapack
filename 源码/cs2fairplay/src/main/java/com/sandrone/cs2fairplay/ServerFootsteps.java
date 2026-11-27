package com.sandrone.cs2fairplay;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class ServerFootsteps {
    private static final Map<ServerPlayer, FootstepWire.Policy> SENT = new IdentityHashMap<>();
    /** 单独开启塔科夫脚步的玩家（不在队伍里也生效），重启回落。 */
    private static final Set<UUID> ALLOWED = new HashSet<>();

    private ServerFootsteps() {}

    public static int allow(ServerPlayer player, boolean on) {
        if (on) ALLOWED.add(player.getUUID()); else ALLOWED.remove(player.getUUID());
        SENT.clear();
        return on ? 1 : 0;
    }

    public static boolean allowed(ServerPlayer player) {
        return ALLOWED.contains(player.getUUID());
    }

    /** 队伍成员，或被单独开启的玩家；仍要求存活且非旁观。 */
    public static boolean customSource(ServerPlayer player) {
        return (onTeam(player) || allowed(player)) && player.isAlive() && !player.isSpectator()
                && Cs2FairPlay.supportsFootsteps(player);
    }

    @SubscribeEvent
    public static void tick(ServerTickEvent.Post event) {
        var players = event.getServer().getPlayerList();
        SENT.keySet().removeIf(player -> players.getPlayer(player.getUUID()) != player);
        ALLOWED.removeIf(id -> players.getPlayer(id) == null);
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
            if (!policy.equals(SENT.put(listener, policy))) {
                PacketDistributor.sendToPlayer(listener, policy);
            }
        }
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) {
        SENT.clear();
        ALLOWED.clear();
    }

    private static boolean canHear(ServerPlayer player) {
        return onTeam(player) || allowed(player)
                || (player.getTeam() != null && "cs2_cast".equals(player.getTeam().getName()));
    }

    private static boolean onTeam(ServerPlayer player) {
        if (player.getTeam() == null) return false;
        String name = player.getTeam().getName();
        return "cs2_t".equals(name) || "cs2_ct".equals(name);
    }
}
