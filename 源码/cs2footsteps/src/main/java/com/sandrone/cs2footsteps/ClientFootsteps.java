package com.sandrone.cs2footsteps;

import com.sandrone.cs2fairplay.FootstepWire;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Pose;
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
        int parity;
        int jitterTicks;
        boolean wasProne;
        int stepTicks;
        boolean moving;
        int lastLandingTick = -100;
    }

    private static final Map<Player, State> STATES = new HashMap<>();
    private static ClientLevel lastLevel;
    private static LocalPlayer lastLocalPlayer;

    private static final Map<String, Integer> VARIANTS = Map.ofEntries(
            Map.entry("body.prone_down", 4),
            Map.entry("body.prone_up", 4),
            Map.entry("dirt.jump", 2),
            Map.entry("dirt.landing", 2),
            Map.entry("dirt.prone", 4),
            Map.entry("dirt.run", 4),
            Map.entry("dirt.sneak", 4),
            Map.entry("dirt.sprint", 4),
            Map.entry("dirt.walk", 4),
            Map.entry("dry_leaves.jump", 2),
            Map.entry("dry_leaves.landing", 2),
            Map.entry("dry_leaves.prone", 4),
            Map.entry("dry_leaves.run", 4),
            Map.entry("dry_leaves.sneak", 4),
            Map.entry("dry_leaves.sprint", 4),
            Map.entry("dry_leaves.walk", 4),
            Map.entry("fabric.jump", 2),
            Map.entry("fabric.landing", 2),
            Map.entry("fabric.prone", 4),
            Map.entry("fabric.run", 4),
            Map.entry("fabric.sneak", 4),
            Map.entry("fabric.sprint", 4),
            Map.entry("fabric.walk", 4),
            Map.entry("glass.jump", 2),
            Map.entry("glass.landing", 2),
            Map.entry("glass.prone", 4),
            Map.entry("glass.run", 4),
            Map.entry("glass.sneak", 4),
            Map.entry("glass.sprint", 4),
            Map.entry("glass.walk", 4),
            Map.entry("grass.jump", 2),
            Map.entry("grass.landing", 2),
            Map.entry("grass.prone", 4),
            Map.entry("grass.run", 4),
            Map.entry("grass.sneak", 4),
            Map.entry("grass.sprint", 4),
            Map.entry("grass.walk", 4),
            Map.entry("grasslow.jump", 2),
            Map.entry("grasslow.landing", 2),
            Map.entry("grasslow.prone", 4),
            Map.entry("grasslow.run", 4),
            Map.entry("grasslow.sneak", 4),
            Map.entry("grasslow.sprint", 4),
            Map.entry("grasslow.walk", 4),
            Map.entry("gravel.jump", 2),
            Map.entry("gravel.landing", 2),
            Map.entry("gravel.prone", 4),
            Map.entry("gravel.run", 4),
            Map.entry("gravel.sneak", 4),
            Map.entry("gravel.sprint", 4),
            Map.entry("gravel.walk", 4),
            Map.entry("metal.jump", 1),
            Map.entry("metal.landing", 1),
            Map.entry("metal.prone", 4),
            Map.entry("metal.run", 4),
            Map.entry("metal.sneak", 4),
            Map.entry("metal.sprint", 4),
            Map.entry("metal.walk", 4),
            Map.entry("metal2.jump", 1),
            Map.entry("metal2.landing", 1),
            Map.entry("metal2.prone", 1),
            Map.entry("metal2.run", 1),
            Map.entry("metal2.sneak", 1),
            Map.entry("metal2.sprint", 1),
            Map.entry("metal2.walk", 1),
            Map.entry("metal3.jump", 1),
            Map.entry("metal3.landing", 1),
            Map.entry("metal3.prone", 1),
            Map.entry("metal3.run", 1),
            Map.entry("metal3.sneak", 1),
            Map.entry("metal3.sprint", 1),
            Map.entry("metal3.walk", 1),
            Map.entry("metal_thin.jump", 2),
            Map.entry("metal_thin.landing", 2),
            Map.entry("metal_thin.prone", 4),
            Map.entry("metal_thin.run", 4),
            Map.entry("metal_thin.sneak", 4),
            Map.entry("metal_thin.sprint", 4),
            Map.entry("metal_thin.walk", 4),
            Map.entry("mud.jump", 2),
            Map.entry("mud.landing", 2),
            Map.entry("mud.prone", 4),
            Map.entry("mud.run", 4),
            Map.entry("mud.sneak", 4),
            Map.entry("mud.sprint", 4),
            Map.entry("mud.walk", 4),
            Map.entry("sand.jump", 2),
            Map.entry("sand.landing", 2),
            Map.entry("sand.prone", 4),
            Map.entry("sand.run", 4),
            Map.entry("sand.sneak", 4),
            Map.entry("sand.sprint", 4),
            Map.entry("sand.walk", 4),
            Map.entry("snow.jump", 2),
            Map.entry("snow.landing", 2),
            Map.entry("snow.prone", 4),
            Map.entry("snow.run", 4),
            Map.entry("snow.sneak", 4),
            Map.entry("snow.sprint", 4),
            Map.entry("snow.walk", 4),
            Map.entry("stone.jump", 2),
            Map.entry("stone.landing", 2),
            Map.entry("stone.prone", 4),
            Map.entry("stone.run", 4),
            Map.entry("stone.sneak", 4),
            Map.entry("stone.sprint", 4),
            Map.entry("stone.walk", 4),
            Map.entry("water.jump", 2),
            Map.entry("water.landing", 2),
            Map.entry("water.prone", 4),
            Map.entry("water.run", 4),
            Map.entry("water.sneak", 4),
            Map.entry("water.sprint", 4),
            Map.entry("water.walk", 4),
            Map.entry("whole_glass.jump", 2),
            Map.entry("whole_glass.landing", 2),
            Map.entry("whole_glass.prone", 4),
            Map.entry("whole_glass.run", 4),
            Map.entry("whole_glass.sneak", 4),
            Map.entry("whole_glass.sprint", 4),
            Map.entry("whole_glass.walk", 4),
            Map.entry("wood.jump", 2),
            Map.entry("wood.landing", 2),
            Map.entry("wood.prone", 4),
            Map.entry("wood.run", 4),
            Map.entry("wood.sneak", 4),
            Map.entry("wood.sprint", 4),
            Map.entry("wood.walk", 4),
            Map.entry("wood_thin.jump", 2),
            Map.entry("wood_thin.landing", 2),
            Map.entry("wood_thin.prone", 4),
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

    public static boolean replacesFootsteps(Player player) {
        return player.level().isClientSide() && !player.isSpectator() && !player.isDeadOrDying()
                && FootstepWire.permits(player.level().dimension().location(), player.getId());
    }

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
            if (!replacesFootsteps(p)) {
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
        boolean prone = p.getPose() == Pose.SWIMMING && !p.isInWater();
        if (prone != st.wasProne) {
            st.wasProne = prone;
            playBody(p, level, st, prone ? "prone_down" : "prone_up");
        }
        if (prone) {
            state = "prone";
            interval = 18;
        } else if (p.isShiftKeyDown()) {
            state = "sneak";
            interval = 18;
        } else if (p.isSprinting()) {
            state = "sprint";
            interval = 6;
        } else {
            state = "run";
            interval = 8;
        }
        // 步频按 tick 计时，不受速度效果影响；水中放慢
        if (isWaterAt(level, p)) interval = Math.round(interval * 1.6f);
        boolean moving = onGround && moved > 0.03 && moved < 1.5;
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
            if (st.stepTicks >= interval + st.jitterTicks && now - st.lastPlayedTick >= 1) {
                st.stepTicks = 0;
                st.lastPlayedTick = now;
                st.jitterTicks = level.getRandom().nextInt(4) == 0 ? 1 : 0;
                play(p, level, st, state, 1.0f, 0.03, false);
            }
        }
        st.last = pos;
        st.wasOnGround = onGround;
    }

    private static void play(Player p, ClientLevel level, State st, String state,
                             float gain, double pitchJitter, boolean firstStep) {
        MatInfo mat = materialOf(level, p.getOnPos());
        if ("prone".equals(state) && !VARIANTS.containsKey(mat.id + ".prone")) {
            state = "walk";
            gain *= 0.45f;
        } else if ("prone".equals(state)) {
            gain *= 0.9f;
        }
        String variantKey = mat.id + "." + state;
        int variants = Math.max(1, VARIANTS.getOrDefault(variantKey, 4));
        boolean stepState = "run".equals(state) || "sprint".equals(state) || "sneak".equals(state);
        int variant;
        if (stepState && variants >= 4) {
            // 左右脚交替，各用一半变体，节奏和音色更规律
            int half = variants / 2;
            int side = st.parity++ & 1;
            variant = 1 + side * half + level.getRandom().nextInt(half);
        } else {
            variant = 1 + level.getRandom().nextInt(variants);
            if (firstStep && variants > 1) {
                variant = 1;
            }
        }
        if (variants > 1) {
            Integer last = st.lastVariant.get(variantKey);
            if (last != null && last == variant && variants > 1) {
                variant = variant % variants + 1;
            }
            st.lastVariant.put(variantKey, variant);
        }
        String id = "step." + mat.id + "." + state + "." + variant;
        SoundEvent sound = SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(Cs2Footsteps.MODID, id));
        Vec3 vel = p.position().subtract(st.last);
        double x = p.getX() + vel.x * 0.18;
        double z = p.getZ() + vel.z * 0.18;
        double y = p.getY() + 0.05;
        float pitch = (float) (1.0 + (level.getRandom().nextDouble() - 0.5) * 0.012)
                * mat.pitchMul * (firstStep ? 1.03f : 1.0f);
        float finalGain = gain * mat.gainMul * (firstStep ? 1.05f : 1.0f)
                * (float) (0.985 + level.getRandom().nextDouble() * 0.03);
        level.playLocalSound(x, y, z, sound, SoundSource.PLAYERS, finalGain, pitch, false);
    }

    private record MatInfo(String id, float pitchMul, float gainMul) {
    }

    private static boolean isWaterAt(ClientLevel level, Player p) {
        BlockPos feet = p.blockPosition();
        return !level.getBlockState(feet).getFluidState().isEmpty()
                || !level.getBlockState(feet.above()).getFluidState().isEmpty();
    }

    private static void playBody(Player p, ClientLevel level, State st, String kind) {
        String variantKey = "body." + kind;
        int variants = Math.max(1, VARIANTS.getOrDefault(variantKey, 4));
        int variant = 1 + level.getRandom().nextInt(variants);
        String id = "step.body." + kind + "." + variant;
        SoundEvent sound = SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(Cs2Footsteps.MODID, id));
        Vec3 vel = p.getDeltaMovement();
        double x = p.getX() + vel.x * 0.1;
        double z = p.getZ() + vel.z * 0.1;
        level.playLocalSound(x, p.getY() + 0.1, z, sound, SoundSource.PLAYERS, 0.9f, 1.0f, false);
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
        if (!state.getFluidState().isEmpty()
                || !level.getBlockState(pos.above()).getFluidState().isEmpty()) {
            return new MatInfo("water", 0.96f, 1.0f);
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
        if (sound == SoundType.LADDER || sound == SoundType.SCAFFOLDING) {
            return new MatInfo("wood_thin", 1.0f, 1.0f);
        }
        if (sound == SoundType.ANVIL || sound == SoundType.NETHERITE_BLOCK
                || sound == SoundType.HEAVY_CORE || sound == SoundType.VAULT
                || sound == SoundType.TRIAL_SPAWNER) {
            return new MatInfo("metal2", 1.0f, 1.0f);
        }
        if (sound == SoundType.CHAIN || sound == SoundType.COPPER_GRATE) {
            return new MatInfo("metal_thin", 1.0f, 1.0f);
        }
        if (sound == SoundType.COPPER || sound == SoundType.COPPER_BULB) {
            return new MatInfo("metal3", 1.0f, 1.0f);
        }
        if (sound == SoundType.WOOD || sound == SoundType.BAMBOO || sound == SoundType.BAMBOO_WOOD
                || sound == SoundType.CHERRY_WOOD || sound == SoundType.NETHER_WOOD) {
            return (path.contains("trapdoor") || path.contains("ladder") || path.contains("door"))
                    ? new MatInfo("wood_thin", 1.0f, 1.0f)
                    : new MatInfo("wood", 1.0f, 1.0f);
        }
        if (sound == SoundType.GRASS || sound == SoundType.WET_GRASS) {
            if (path.contains("leaves")) {
                return new MatInfo("dry_leaves", 1.0f, 1.0f);
            }
            if (path.contains("short_grass") || path.contains("fern") || path.contains("azalea")
                    || path.contains("bush") || path.contains("tall_grass")) {
                return new MatInfo("grasslow", 1.0f, 1.0f);
            }
            return new MatInfo("grass", 1.0f, 1.0f);
        }
        if (sound == SoundType.AZALEA || sound == SoundType.FLOWERING_AZALEA
                || sound == SoundType.MOSS || sound == SoundType.MOSS_CARPET
                || sound == SoundType.PINK_PETALS || sound == SoundType.BIG_DRIPLEAF
                || sound == SoundType.SMALL_DRIPLEAF || sound == SoundType.ROOTS
                || sound == SoundType.HANGING_ROOTS || sound == SoundType.VINE
                || sound == SoundType.CAVE_VINES || sound == SoundType.CROP
                || sound == SoundType.HARD_CROP || sound == SoundType.NETHER_WART
                || sound == SoundType.SWEET_BERRY_BUSH || sound == SoundType.BAMBOO_SAPLING) {
            return new MatInfo("grasslow", 1.0f, 1.0f);
        }
        if (sound == SoundType.AZALEA_LEAVES || sound == SoundType.CHERRY_LEAVES) {
            return new MatInfo("grass", 1.0f, 1.0f);
        }
        if (sound == SoundType.GRAVEL) {
            if (path.contains("mud")) {
                return new MatInfo("mud", 1.0f, 1.0f);
            }
            if (path.contains("dirt") || path.contains("farmland") || path.contains("podzol")
                    || path.contains("mycelium") || path.contains("clay") || path.contains("rooted")) {
                return new MatInfo("dirt", 1.0f, 1.0f);
            }
            return new MatInfo("gravel", 1.0f, 1.0f);
        }
        if (sound == SoundType.SAND || sound == SoundType.SOUL_SAND
                || sound == SoundType.SUSPICIOUS_SAND) {
            return new MatInfo("sand", 1.04f, 0.9f);
        }
        if (sound == SoundType.SUSPICIOUS_GRAVEL) {
            return new MatInfo("gravel", 1.0f, 1.0f);
        }
        if (sound == SoundType.GLASS || sound == SoundType.AMETHYST
                || sound == SoundType.AMETHYST_CLUSTER || sound == SoundType.SMALL_AMETHYST_BUD
                || sound == SoundType.MEDIUM_AMETHYST_BUD || sound == SoundType.LARGE_AMETHYST_BUD) {
            return new MatInfo("whole_glass", 1.0f, 1.0f);
        }
        if (sound == SoundType.WOOL || sound == SoundType.SLIME_BLOCK
                || sound == SoundType.HONEY_BLOCK || sound == SoundType.SPONGE
                || sound == SoundType.WET_SPONGE || sound == SoundType.COBWEB
                || sound == SoundType.CANDLE || sound == SoundType.SHROOMLIGHT
                || sound == SoundType.FROGLIGHT) {
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
        if (sound == SoundType.POWDER_SNOW) {
            return new MatInfo("snow", 1.15f, 0.7f);
        }
        if (sound == SoundType.LILY_PAD) {
            return new MatInfo("water", 0.96f, 1.0f);
        }
        if (sound == SoundType.MUD || sound == SoundType.PACKED_MUD
                || sound == SoundType.MUD_BRICKS || sound == SoundType.MUDDY_MANGROVE_ROOTS
                || sound == SoundType.ROOTED_DIRT || sound == SoundType.SOUL_SOIL) {
            return new MatInfo("dirt", 1.0f, 1.0f);
        }
        // 模组自定义 SoundType：按其 step 音效 id 兜底
        String step = sound.getStepSound().getLocation().getPath();
        if (step.contains("wood") || step.contains("bamboo") || step.contains("cherry")) {
            return new MatInfo("wood", 1.0f, 1.0f);
        }
        if (step.contains("gravel")) return new MatInfo("gravel", 1.0f, 1.0f);
        if (step.contains("sand")) return new MatInfo("sand", 1.04f, 0.9f);
        if (step.contains("grass")) return new MatInfo("grass", 1.0f, 1.0f);
        if (step.contains("glass")) return new MatInfo("whole_glass", 1.0f, 1.0f);
        if (step.contains("wool") || step.contains("cloth")) return new MatInfo("fabric", 1.0f, 1.0f);
        if (step.contains("snow")) return new MatInfo("snow", 1.0f, 1.0f);
        if (step.contains("metal") || step == "block.chain.step") return new MatInfo("metal", 1.0f, 1.0f);
        return new MatInfo("stone", 1.0f, 1.0f);
    }
}
