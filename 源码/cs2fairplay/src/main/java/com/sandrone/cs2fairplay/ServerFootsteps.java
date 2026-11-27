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
    private static boolean allSources = false;

    /** 是否让非比赛队伍玩家也使用塔科夫脚步。默认关闭，重启回落。 */
    public static int setAllSources(boolean on) {
        allSources = on;
        SENT.clear();
        return on ? 1 : 0;
    }

    public static boolean allSources() {
        return allSources;
    }

    private ServerFootsteps() {}

    /** 只看是否在比赛队伍：不判断比赛阶段，也不看 cs2_alive。 */
    public static boolean customSource(ServerPlayer player) {
        return (allSources || onTeam(player)) && player.isAlive() && !player.isSpectator()
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
        if (allSources) return true;
        return onTeam(player)
                || (player.getTeam() != null && "cs2_cast".equals(player.getTeam().getName()));
    }

    private static boolean onTeam(ServerPlayer player) {
        if (player.getTeam() == null) return false;
        String name = player.getTeam().getName();
        return "cs2_t".equals(name) || "cs2_ct".equals(name);
    }
}
