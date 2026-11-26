package com.sandrone.cs2fairplay;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;

import static com.sandrone.cs2fairplay.FootstepWire.*;

public final class ServerFootsteps {
    private static final int EVIDENCE_TICKS = 6;
    private static final Map<ServerPlayer, Movement> TRACKED = new IdentityHashMap<>();

    private ServerFootsteps() {}

    private static final class Movement {
        final int[] requests = new int[3];
        ServerLevel level;
        Vec3 previous;
        double groundY;
        double peakY;
        double distance;
        boolean grounded;
        int lastMoved = -100;
        int lastStep = -100;
        int jump = -100;
        int landing = -100;
    }

    public static boolean customSource(ServerPlayer player) {
        return MatchFairPlay.participant(player) && player.isAlive() && !player.isSpectator()
                && MatchFairPlay.score(player.getServer(), player, "cs2_alive") == 1
                && Cs2FairPlay.supportsFootsteps(player);
    }

    static void request(ServerPlayer player, int motion) {
        if (motion < STEP || motion > LANDING || !customSource(player)) return;
        Movement state = TRACKED.get(player);
        if (state != null) state.requests[motion] = player.getServer().getTickCount() + EVIDENCE_TICKS;
    }

    @SubscribeEvent
    public static void tick(ServerTickEvent.Post event) {
        var server = event.getServer();
        var players = server.getPlayerList();
        TRACKED.keySet().removeIf(player -> players.getPlayer(player.getUUID()) != player);
        int now = server.getTickCount();
        for (ServerPlayer player : players.getPlayers()) {
            Movement state = TRACKED.get(player);
            if (!customSource(player)) {
                if (TRACKED.remove(player) != null && Cs2FairPlay.supportsFootsteps(player)) {
                    PacketDistributor.sendToPlayer(player, new Policy(false));
                }
                continue;
            }
            if (state == null) {
                state = new Movement();
                TRACKED.put(player, state);
                PacketDistributor.sendToPlayer(player, new Policy(true));
            }
            observe(player, state, now);
        }
    }

    @SubscribeEvent
    public static void stopped(ServerStoppedEvent event) { TRACKED.clear(); }

    private static void observe(ServerPlayer player, Movement state, int now) {
        Vec3 position = player.position();
        boolean grounded = player.onGround();
        if (state.level != player.serverLevel() || state.previous == null || state.previous.distanceToSqr(position) >= 2.25) {
            state.level = player.serverLevel();
            state.previous = position;
            state.groundY = position.y;
            state.peakY = position.y;
            state.grounded = grounded;
            state.distance = 0;
            state.lastMoved = state.jump = state.landing = -100;
            Arrays.fill(state.requests, 0);
            return;
        }
        Vec3 delta = position.subtract(state.previous);
        if (state.grounded && !grounded) {
            if (delta.y > 0.15) state.jump = now;
            state.peakY = position.y;
            state.distance = 0;
        } else if (!state.grounded && grounded) {
            if (state.peakY - position.y > 0.7 || position.y > state.groundY + 0.4) state.landing = now;
        }
        if (grounded) {
            state.groundY = position.y;
            double moved = delta.horizontalDistance();
            if (moved > 0.001) {
                state.distance = Math.min(1.55, state.distance + moved);
                state.lastMoved = now;
            }
        } else {
            state.peakY = Math.max(state.peakY, position.y);
        }

        if (pending(state, STEP, now) && grounded && state.distance >= (player.isShiftKeyDown() ? 0.48 : 0.60)
                && now - state.lastMoved <= 3 && now - state.lastStep >= 2) {
            state.requests[STEP] = 0;
            state.distance = 0;
            state.lastStep = now;
            send(player, delta, player.isShiftKeyDown() ? SNEAK : player.isSprinting() ? SPRINT : STEP);
        }
        if (pending(state, JUMP, now) && now - state.jump <= EVIDENCE_TICKS) {
            state.requests[JUMP] = 0;
            state.jump = -100;
            send(player, delta, JUMP);
        }
        if (pending(state, LANDING, now) && now - state.landing <= EVIDENCE_TICKS) {
            state.requests[LANDING] = 0;
            state.landing = -100;
            send(player, delta, LANDING);
        }
        state.previous = position;
        state.grounded = grounded;
    }

    private static boolean pending(Movement state, int motion, int now) {
        return state.requests[motion] != 0 && state.requests[motion] >= now;
    }

    private static void send(ServerPlayer source, Vec3 movement, int motion) {
        Vec3 at = source.position().add(movement.x * 0.18, 0.05, movement.z * 0.18);
        // Corresponds to the attenuation distances supplied by the step assets.
        int radius = switch (motion) {
            case JUMP -> 12;
            case LANDING -> 22;
            case SNEAK -> 9;
            case SPRINT -> 26;
            default -> 20;
        };
        var cue = new Cue(source.getId(), motion, source.getOnPos(), at.x, at.y, at.z);
        for (ServerPlayer listener : source.serverLevel().players()) {
            if (canHear(listener) && listener.getCamera().position().distanceToSqr(at) <= radius * radius
                    && Cs2FairPlay.supportsFootsteps(listener)) {
                PacketDistributor.sendToPlayer(listener, cue);
            }
        }
    }

    private static boolean canHear(ServerPlayer player) {
        return MatchFairPlay.participant(player)
                || (player.getTeam() != null && "cs2_cast".equals(player.getTeam().getName()));
    }
}
