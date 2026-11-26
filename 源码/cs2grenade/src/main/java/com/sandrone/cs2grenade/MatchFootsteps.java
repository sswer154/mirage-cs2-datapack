package com.sandrone.cs2grenade;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;

/** Sends spatial footsteps for active participants without requiring a client mod. */
public final class MatchFootsteps {
    private static final ScoreHolder MATCH_STATE = ScoreHolder.forNameOnly("#state");
    private static final double MAX_MOVEMENT_PER_TICK = 3.0;
    private final Map<UUID, Stride> strides = new HashMap<>();

    enum Motion {
        WALK("walk", 22, 0.85f, 1.6, 8),
        RUN("run", 32, 1.0f, 1.75, 5),
        SNEAK("sneak", 6, 0.45f, 0.85, 10),
        LAND("land", 26, 1.0f, 0.0, 0);

        final String key;
        final int range;
        final float volume;
        final double stride;
        final int minTicks;

        Motion(String key, int range, float volume, double stride, int minTicks) {
            this.key = key;
            this.range = range;
            this.volume = volume;
            this.stride = stride;
            this.minTicks = minTicks;
        }
    }

    private static final class Stride {
        final ServerPlayer player;
        final ServerLevel level;
        Vec3 lastPosition;
        boolean wasOnGround;
        boolean wasDescending;
        double distance;
        int lastStep;
        Motion lastMotion;

        Stride(ServerPlayer player, int now) {
            this.player = player;
            level = player.serverLevel();
            lastPosition = player.position();
            wasOnGround = player.onGround();
            lastStep = now - Motion.SNEAK.minTicks;
        }
    }

    void tick(MinecraftServer server, boolean active) {
        if (!active) {
            strides.clear();
            return;
        }
        int now = server.getTickCount();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            var id = player.getUUID();
            if (!canEmit(player) || player.isPassenger() || player.getAbilities().flying || player.isSwimming()) {
                strides.remove(id);
                continue;
            }

            Stride stride = strides.get(id);
            Vec3 position = player.position();
            if (stride == null || stride.player != player || stride.level != player.serverLevel()
                    || stride.lastPosition.distanceToSqr(position) > MAX_MOVEMENT_PER_TICK * MAX_MOVEMENT_PER_TICK) {
                strides.put(id, new Stride(player, now));
                continue;
            }

            Vec3 delta = position.subtract(stride.lastPosition);
            boolean grounded = player.onGround();
            boolean landed = grounded && !stride.wasOnGround && (delta.y < -0.02 || stride.wasDescending);
            boolean walkingOnGround = grounded && stride.wasOnGround;
            stride.lastPosition = position;
            stride.wasOnGround = grounded;
            stride.wasDescending = !grounded && delta.y < -0.02;

            if (landed) {
                stride.distance = 0.0;
                stride.lastStep = now;
                play(player, Motion.LAND);
                continue;
            }
            if (!walkingOnGround) {
                stride.distance = 0.0;
                continue;
            }

            Motion motion = player.isShiftKeyDown() ? Motion.SNEAK : player.isSprinting() ? Motion.RUN : Motion.WALK;
            if (stride.lastMotion != motion) {
                stride.distance = 0.0;
                stride.lastMotion = motion;
            }
            double distance = delta.horizontalDistance();
            if (distance < 0.01) {
                continue;
            }
            stride.distance = Math.min(motion.stride, stride.distance + distance);
            if (stride.distance >= motion.stride && now - stride.lastStep >= motion.minTicks) {
                stride.distance = 0.0;
                stride.lastStep = now;
                play(player, motion);
            }
        }
    }

    void remove(ServerPlayer player) {
        strides.remove(player.getUUID());
    }

    private static void play(ServerPlayer player, Motion motion) {
        ServerLevel level = player.serverLevel();
        Vec3 position = player.position();
        var support = player.getOnPos();
        String material = player.isInWater() ? "water" : material(
                level.getBlockState(support).getSoundType(level, support, player).getStepSound().getLocation().getPath());
        SoundEvent sound = SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath("cs2", "step." + material + "." + motion.key));
        double rangeSqr = (double) motion.range * motion.range;
        long seed = level.getRandom().nextLong();
        for (ServerPlayer listener : level.players()) {
            if (listener != player && canHear(listener) && listener.getCamera().distanceToSqr(position) < rangeSqr) {
                GrenadeManager.sendSoundPacket(listener, sound, position, motion.volume, 1.0f, seed);
            }
        }
    }

    static String material(String stepSound) {
        return switch (stepSound) {
            case "block.wood.step", "block.bamboo_wood.step", "block.cherry_wood.step",
                    "block.nether_wood.step", "block.stem.step" -> "wood";
            case "block.metal.step", "block.chain.step", "block.copper.step",
                    "block.netherite_block.step", "block.lantern.step", "block.iron.step" -> "metal";
            case "block.sand.step", "block.gravel.step", "block.soul_sand.step",
                    "block.soul_soil.step", "block.mud.step", "block.muddy_mangrove_roots.step",
                    "block.rooted_dirt.step", "block.dirt.step" -> "grit";
            case "block.grass.step", "block.moss.step", "block.moss_carpet.step",
                    "block.azalea_leaves.step", "block.azalea.step", "block.wet_grass.step" -> "grass";
            case "block.glass.step", "block.amethyst_block.step", "block.amethyst_cluster.step",
                    "block.tuff_bricks.step", "block.polished_tuff.step", "block.decorated_pot.step" -> "glass";
            case "block.snow.step", "block.powder_snow.step" -> "snow";
            default -> "stone";
        };
    }

    /** Let the local client keep its own immediate footsteps; replace server broadcasts. */
    public static boolean suppressVanilla(ServerPlayer player) {
        int phase = score(player.getScoreboard(), MATCH_STATE, "cs2_state");
        return phase >= 2 && phase <= 4 && canEmit(player);
    }

    private static boolean canEmit(ServerPlayer player) {
        return player.isAlive() && !player.isSpectator() && isParticipant(player)
                && score(player.getScoreboard(), player, "cs2_alive") == 1;
    }

    private static boolean canHear(ServerPlayer player) {
        var team = player.getTeam();
        return isParticipant(player) || (team != null && "cs2_cast".equals(team.getName()));
    }

    private static boolean isParticipant(ServerPlayer player) {
        return score(player.getScoreboard(), player, "cs2_inmatch") == 1;
    }

    private static int score(Scoreboard board, ScoreHolder holder, String name) {
        var objective = board.getObjective(name);
        var score = objective == null ? null : board.getPlayerScoreInfo(holder, objective);
        return score == null ? 0 : score.value();
    }
}
