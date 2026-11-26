package com.sandrone.cs2footsteps;

import com.sandrone.cs2fairplay.FootstepWire;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

/** Computes local step cadence and resolves authorized cues to client sound assets. */
public final class ClientFootsteps {
    private ClientFootsteps() {
    }

    private static final class State {
        Vec3 last = null;
        double accum = 0.0;
        boolean wasOnGround = true;
        double airFall = 0.0;
        double lastGroundY = Double.NaN;
        int lastPlayedTick = -100;
        int lastJumpTick = -100;
    }

    private static final class Playback {
        long lastStep = -100;
        long lastSound;
        final Map<String, Integer> lastVariant = new HashMap<>();
    }

    private static final Map<Integer, Playback> PLAYBACK = new HashMap<>();
    private static State movement = new State();
    private static ClientLevel lastLevel;
    private static LocalPlayer lastLocalPlayer;

    private static final Map<String, Integer> VARIANTS = Map.ofEntries(
            Map.entry("dirt.jump", 2),
            Map.entry("dirt.landing", 2),
            Map.entry("dirt.run", 4),
            Map.entry("dirt.sneak", 4),
            Map.entry("dirt.sprint", 4),
            Map.entry("dirt.walk", 4),
            Map.entry("fabric.jump", 2),
            Map.entry("fabric.landing", 2),
            Map.entry("fabric.run", 4),
            Map.entry("fabric.sneak", 4),
            Map.entry("fabric.sprint", 4),
            Map.entry("fabric.walk", 4),
            Map.entry("glass.jump", 2),
            Map.entry("glass.landing", 2),
            Map.entry("glass.run", 4),
            Map.entry("glass.sneak", 4),
            Map.entry("glass.sprint", 4),
            Map.entry("glass.walk", 4),
            Map.entry("grass.jump", 2),
            Map.entry("grass.landing", 2),
            Map.entry("grass.run", 4),
            Map.entry("grass.sneak", 4),
            Map.entry("grass.sprint", 4),
            Map.entry("grass.walk", 4),
            Map.entry("grasslow.jump", 2),
            Map.entry("grasslow.landing", 2),
            Map.entry("grasslow.run", 4),
            Map.entry("grasslow.sneak", 4),
            Map.entry("grasslow.sprint", 4),
            Map.entry("grasslow.walk", 4),
            Map.entry("gravel.jump", 2),
            Map.entry("gravel.landing", 2),
            Map.entry("gravel.run", 4),
            Map.entry("gravel.sneak", 4),
            Map.entry("gravel.sprint", 4),
            Map.entry("gravel.walk", 4),
            Map.entry("metal.jump", 2),
            Map.entry("metal.landing", 2),
            Map.entry("metal.run", 4),
            Map.entry("metal.sneak", 4),
            Map.entry("metal.sprint", 3),
            Map.entry("metal.walk", 4),
            Map.entry("metal2.jump", 1),
            Map.entry("metal2.landing", 1),
            Map.entry("metal2.run", 1),
            Map.entry("metal2.sneak", 1),
            Map.entry("metal2.sprint", 1),
            Map.entry("metal2.walk", 1),
            Map.entry("metal3.jump", 1),
            Map.entry("metal3.landing", 1),
            Map.entry("metal3.run", 1),
            Map.entry("metal3.sneak", 1),
            Map.entry("metal3.sprint", 1),
            Map.entry("metal3.walk", 1),
            Map.entry("metal_thin.jump", 2),
            Map.entry("metal_thin.landing", 2),
            Map.entry("metal_thin.run", 4),
            Map.entry("metal_thin.sneak", 4),
            Map.entry("metal_thin.sprint", 4),
            Map.entry("metal_thin.walk", 4),
            Map.entry("snow.jump", 2),
            Map.entry("snow.landing", 2),
            Map.entry("snow.run", 4),
            Map.entry("snow.sneak", 4),
            Map.entry("snow.sprint", 4),
            Map.entry("snow.walk", 4),
            Map.entry("stone.jump", 2),
            Map.entry("stone.landing", 2),
            Map.entry("stone.run", 4),
            Map.entry("stone.sneak", 4),
            Map.entry("stone.sprint", 4),
            Map.entry("stone.walk", 4),
            Map.entry("water.jump", 2),
            Map.entry("water.landing", 2),
            Map.entry("water.run", 4),
            Map.entry("water.sneak", 4),
            Map.entry("water.sprint", 4),
            Map.entry("water.walk", 4),
            Map.entry("wood.jump", 2),
            Map.entry("wood.landing", 2),
            Map.entry("wood.run", 4),
            Map.entry("wood.sneak", 4),
            Map.entry("wood.sprint", 4),
            Map.entry("wood.walk", 4),
            Map.entry("wood_thin.jump", 2),
            Map.entry("wood_thin.landing", 2),
            Map.entry("wood_thin.run", 4),
            Map.entry("wood_thin.sneak", 4),
            Map.entry("wood_thin.sprint", 4),
            Map.entry("wood_thin.walk", 4)
    );


    private static final double STEP_SNEAK = 1.20;
    private static final double STEP_WALK = 1.55;

    // sounds.json defines attenuation independently of the playback gain.
    private static final float GAIN_JUMP = 0.75f;

    static void tick() {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        LocalPlayer localPlayer = mc.player;
        if (lastLevel != level || lastLocalPlayer != localPlayer) {
            PLAYBACK.clear();
            movement = new State();
            lastLevel = level;
            lastLocalPlayer = localPlayer;
        }
        if (level == null || localPlayer == null || mc.getConnection() == null
                || !NetworkRegistry.hasChannel(mc.getConnection(), FootstepWire.Request.TYPE.id())) {
            FootstepWire.resetLocal();
            return;
        }
        if (localPlayer.tickCount % 40 == 0) {
            long now = level.getGameTime();
            PLAYBACK.values().removeIf(state -> now - state.lastSound > 200);
        }
        if (!FootstepWire.localCustom() || localPlayer.isSpectator() || !localPlayer.isAlive()) {
            if (movement.last != null) movement = new State();
            return;
        }
        if (!mc.isPaused()) step(localPlayer, localPlayer.tickCount);
    }

    private static void step(Player p, int now) {
        State st = movement;
        Vec3 pos = p.position();
        boolean onGround = p.onGround();
        Vec3 vel = p.getDeltaMovement();

        if (st.last == null || st.last.distanceToSqr(pos) >= 2.25) {
            st.last = pos;
            st.accum = 0;
            st.airFall = 0;
            st.wasOnGround = onGround;
            st.lastGroundY = pos.y;
            return;
        }

        if (st.wasOnGround && !onGround && vel.y > 0.15 && now - st.lastJumpTick > 8) {
            st.lastJumpTick = now;
            PacketDistributor.sendToServer(new FootstepWire.Request(FootstepWire.JUMP));
        }
        if (!onGround) {
            st.airFall = Math.max(st.airFall, p.fallDistance);
            st.accum = 0.0;
        } else if (!st.wasOnGround) {
            boolean steppedUp = !Double.isNaN(st.lastGroundY) && pos.y > st.lastGroundY + 0.4;
            if (st.airFall > 0.7 || steppedUp) {
                PacketDistributor.sendToServer(new FootstepWire.Request(FootstepWire.LANDING));
            }
            st.airFall = 0.0;
        }
        if (onGround) {
            st.lastGroundY = pos.y;
        }

        double dx = pos.x - st.last.x;
        double dz = pos.z - st.last.z;
        double moved = Math.sqrt(dx * dx + dz * dz);
        double stepLen = p.isShiftKeyDown() ? STEP_SNEAK : STEP_WALK;
        if (onGround && moved > 0.001 && moved < 1.5) {
            if (st.accum == 0.0 && moved > 0.02 && now - st.lastPlayedTick > 6) {
                st.accum = stepLen * 0.55;
            }
            st.accum += moved;
        }
        if (st.accum >= stepLen && now - st.lastPlayedTick >= 2) {
            st.accum = 0.0;
            st.lastPlayedTick = now;
            PacketDistributor.sendToServer(new FootstepWire.Request(FootstepWire.STEP));
        }
        st.last = pos;
        st.wasOnGround = onGround;
    }

    static void play(FootstepWire.Cue cue) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return;
        String state = switch (cue.motion()) {
            case FootstepWire.JUMP -> "jump";
            case FootstepWire.LANDING -> "landing";
            case FootstepWire.SNEAK -> "sneak";
            case FootstepWire.SPRINT -> "sprint";
            default -> "run";
        };
        long now = level.getGameTime();
        var st = PLAYBACK.computeIfAbsent(cue.source(), unused -> new Playback());
        st.lastSound = now;
        boolean walking = cue.motion() != FootstepWire.JUMP && cue.motion() != FootstepWire.LANDING;
        boolean firstStep = walking && now - st.lastStep > 20;
        if (walking) st.lastStep = now;
        float gain = cue.motion() == FootstepWire.JUMP ? GAIN_JUMP : 1.0f;
        double pitchJitter = walking ? 0.03 : cue.motion() == FootstepWire.JUMP ? 0.02 : 0.0;
        MatInfo mat = materialOf(level, cue.support());
        String variantKey = mat.id + "." + state;
        int variants = Math.max(1, VARIANTS.getOrDefault(variantKey, 4));
        int variant = 1 + level.getRandom().nextInt(variants);
        if (firstStep && variants > 1) {
            variant = 1;
        } else if (variants > 1) {
            Integer last = st.lastVariant.get(variantKey);
            if (last != null && last == variant) {
                variant = variant % variants + 1;
            }
        }
        if (variants > 1) {
            st.lastVariant.put(variantKey, variant);
        }
        String id = "step." + mat.id + "." + state + "." + variant;
        SoundEvent sound = SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(Cs2Footsteps.MODID, id));
        float pitch = (float) (1.0 + (level.getRandom().nextDouble() - 0.5) * pitchJitter * 2)
                * mat.pitchMul * (firstStep ? 1.06f : 1.0f);
        float finalGain = gain * mat.gainMul * (firstStep ? 1.08f : 1.0f);
        level.playLocalSound(cue.x(), cue.y(), cue.z(), sound, SoundSource.PLAYERS, finalGain, pitch, false);
    }

    private record MatInfo(String id, float pitchMul, float gainMul) {
    }

    private static MatInfo materialOf(ClientLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        SoundType sound = state.getSoundType();
        String path = block.builtInRegistryHolder().key().location().getPath();
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)
                && state.getValue(BlockStateProperties.WATERLOGGED)) {
            return new MatInfo("water", 1.0f, 0.9f);
        }
        if (sound == SoundType.METAL) {
            if (path.contains("bars") || path.contains("chain") || path.contains("grate")
                    || path.contains("rail")) {
                return new MatInfo("metal_thin", 1.0f, 1.0f);
            }
            if (path.contains("copper")) {
                return new MatInfo("metal3", 1.0f, 1.0f);
            }
            if (path.contains("anvil") || path.contains("hopper") || path.contains("cauldron")
                    || path.contains("lantern") || path.contains("bell")) {
                return new MatInfo("metal2", 1.0f, 1.0f);
            }
            return new MatInfo("metal", 1.0f, 1.0f);
        }
        if (sound == SoundType.WOOD) {
            return (path.contains("trapdoor") || path.contains("ladder") || path.contains("door"))
                    ? new MatInfo("wood_thin", 1.0f, 1.0f)
                    : new MatInfo("wood", 1.0f, 1.0f);
        }
        if (sound == SoundType.GRASS) {
            if (path.contains("short_grass") || path.contains("fern") || path.contains("azalea")
                    || path.contains("bush") || path.contains("tall_grass")) {
                return new MatInfo("grasslow", 1.0f, 1.0f);
            }
            return new MatInfo("grass", 1.0f, 1.0f);
        }
        if (sound == SoundType.GRAVEL) {
            return new MatInfo("gravel", 1.0f, 1.0f);
        }
        if (sound == SoundType.SAND) {
            return new MatInfo("dirt", 1.0f, 1.0f);
        }
        if (sound == SoundType.GLASS) {
            return new MatInfo("glass", 1.0f, 1.0f);
        }
        if (sound == SoundType.WOOL) {
            return new MatInfo("fabric", 1.0f, 1.0f);
        }
        if (sound == SoundType.SNOW) {
            if (block instanceof SnowLayerBlock) {
                int layers = state.getValue(SnowLayerBlock.LAYERS);
                if (layers <= 2) {
                    return new MatInfo("snow", 1.10f, 0.8f);
                }
            }
            return new MatInfo("snow", 1.0f, 1.0f);
        }
        return new MatInfo("stone", 1.0f, 1.0f);
    }

}
