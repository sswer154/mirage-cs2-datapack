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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

/** Detects and plays footsteps using the movement already visible to this client. */
public final class ClientFootsteps {
    private ClientFootsteps() {
    }

    private static final class State {
        Vec3 last = null;
        double accum = 0.0;
        boolean wasOnGround = true;
        double airFall = 0.0;
        double lastGroundY = Double.NaN;
        double peakY;
        int lastPlayedTick = -100;
        int lastJumpTick = -100;
        final Map<String, Integer> lastVariant = new HashMap<>();
        int stepTicks;
        boolean moving;
        int lastLandingTick = -100;
    }

    private static final Map<Player, State> STATES = new HashMap<>();
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
            Map.entry("sand.jump", 2),
            Map.entry("sand.landing", 2),
            Map.entry("sand.run", 4),
            Map.entry("sand.sneak", 4),
            Map.entry("sand.sprint", 4),
            Map.entry("sand.walk", 4),
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
    private static final double STEP_SPRINT = 1.55;

    // sounds.json defines attenuation independently of the playback gain.
    private static final float GAIN_JUMP = 0.75f;

    static void tick() {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        LocalPlayer localPlayer = mc.player;
        if (lastLevel != level || lastLocalPlayer != localPlayer) {
            STATES.clear();
            lastLevel = level;
            lastLocalPlayer = localPlayer;
        }
        if (level == null || localPlayer == null || mc.isPaused()) {
            return;
        }
        var players = level.players();
        STATES.keySet().retainAll(players);
        int now = localPlayer.tickCount;
        for (Player p : players) {
            if (!FootstepWire.permits(level.dimension().location(), p.getId()) || p.isSpectator() || p.isDeadOrDying()) {
                STATES.remove(p);
                continue;
            }
            step(p, level, now);
        }
    }

    private static void step(Player p, ClientLevel level, int now) {
        State st = STATES.computeIfAbsent(p, k -> new State());
        Vec3 pos = p.position();
        boolean onGround = p.onGround();

        if (st.last == null || st.last.distanceToSqr(pos) >= 2.25) {
            st.last = pos;
            st.accum = 0;
            st.airFall = 0;
            st.wasOnGround = onGround;
            st.lastGroundY = pos.y;
            st.peakY = pos.y;
            return;
        }

        if (st.wasOnGround && !onGround && pos.y - st.last.y > 0.15 && now - st.lastJumpTick > 8) {
            st.lastJumpTick = now;
            play(p, level, st, "jump", GAIN_JUMP, 0.02, false);
        }
        if (!onGround) {
            st.peakY = st.wasOnGround ? Math.max(st.lastGroundY, pos.y) : Math.max(st.peakY, pos.y);
            st.airFall = Math.max(st.airFall, p.fallDistance);
            st.accum = 0.0;
        } else if (!st.wasOnGround) {
            boolean steppedUp = !Double.isNaN(st.lastGroundY) && pos.y > st.lastGroundY + 0.4;
            if (Math.max(st.airFall, st.peakY - pos.y) > 0.7 || steppedUp) {
                st.lastLandingTick = now;
                play(p, level, st, "landing", 1.0f, 0.0, false);
            }
            st.airFall = 0.0;
        }
        if (onGround) {
            st.lastGroundY = pos.y;
        }

        double dx = pos.x - st.last.x;
        double dz = pos.z - st.last.z;
        double moved = Math.sqrt(dx * dx + dz * dz);
        String state;
        int interval;
        if (p.isShiftKeyDown()) {
            state = "sneak";
            interval = 18;
        } else if (p.isSprinting()) {
            state = "sprint";
            interval = 5;
        } else {
            state = "run";
            interval = 7;
        }
        // 步频按 tick 计时，不受速度效果影响
        boolean moving = onGround && moved > 0.001 && moved < 1.5;
        if (!moving) {
            st.stepTicks = 0;
            st.moving = false;
        } else {
            if (!st.moving && now - st.lastLandingTick > 2) {
                st.moving = true;
                st.stepTicks = 0;
                st.lastPlayedTick = now;
                play(p, level, st, state, 1.0f, 0.03, true);
            } else if (!st.moving) {
                st.moving = true;
                st.stepTicks = 0;
            }
            st.stepTicks++;
            if (st.stepTicks >= interval && now - st.lastPlayedTick >= 1) {
                st.stepTicks = 0;
                st.lastPlayedTick = now;
                play(p, level, st, state, 1.0f, 0.03, false);
            }
        }
        st.last = pos;
        st.wasOnGround = onGround;
    }

    private static void play(Player p, ClientLevel level, State st, String state,
                             float gain, double pitchJitter, boolean firstStep) {
        MatInfo mat = materialOf(level, p.getOnPos());
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
        Vec3 vel = p.position().subtract(st.last);
        double x = p.getX() + vel.x * 0.18;
        double z = p.getZ() + vel.z * 0.18;
        double y = p.getY() + 0.05;
        float pitch = (float) (1.0 + (level.getRandom().nextDouble() - 0.5) * pitchJitter * 2)
                * mat.pitchMul * (firstStep ? 1.06f : 1.0f);
        float finalGain = gain * mat.gainMul * (firstStep ? 1.08f : 1.0f);
        level.playLocalSound(x, y, z, sound, SoundSource.PLAYERS, finalGain, pitch, false);
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
        if (sound == SoundType.SAND || sound == SoundType.SOUL_SAND) {
            return new MatInfo("sand", 1.04f, 0.9f);
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
