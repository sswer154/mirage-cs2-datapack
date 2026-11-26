package com.sandrone.cs2footsteps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

/** 纯客户端脚步声：本地计算步频、材质与播放，不经过服务端。 */
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
        final Map<String, Integer> lastVariant = new HashMap<>();
    }

    private static final Map<Player, State> STATES = new HashMap<>();
    private static ClientLevel lastLevel;
    private static LocalPlayer lastLocalPlayer;

    private static final Map<String, Integer> VARIANTS = Map.ofEntries(
            Map.entry("dirt.jump", 2),
            Map.entry("dirt.landing", 2),
            Map.entry("dirt.run", 4),
            Map.entry("dirt.sprint", 4),
            Map.entry("dirt.walk", 4),
            Map.entry("fabric.jump", 2),
            Map.entry("fabric.landing", 2),
            Map.entry("fabric.run", 4),
            Map.entry("fabric.sprint", 4),
            Map.entry("fabric.walk", 4),
            Map.entry("glass.jump", 2),
            Map.entry("glass.landing", 2),
            Map.entry("glass.run", 4),
            Map.entry("glass.sprint", 4),
            Map.entry("glass.walk", 4),
            Map.entry("grass.jump", 2),
            Map.entry("grass.landing", 2),
            Map.entry("grass.run", 4),
            Map.entry("grass.sprint", 4),
            Map.entry("grass.walk", 4),
            Map.entry("gravel.jump", 2),
            Map.entry("gravel.landing", 2),
            Map.entry("gravel.run", 4),
            Map.entry("gravel.sprint", 4),
            Map.entry("gravel.walk", 4),
            Map.entry("metal.jump", 2),
            Map.entry("metal.landing", 2),
            Map.entry("metal.run", 4),
            Map.entry("metal.sprint", 3),
            Map.entry("metal.walk", 4),
            Map.entry("metal_thin.jump", 2),
            Map.entry("metal_thin.landing", 2),
            Map.entry("metal_thin.run", 4),
            Map.entry("metal_thin.sprint", 4),
            Map.entry("metal_thin.walk", 4),
            Map.entry("snow.jump", 2),
            Map.entry("snow.landing", 2),
            Map.entry("snow.run", 4),
            Map.entry("snow.sprint", 4),
            Map.entry("snow.walk", 4),
            Map.entry("stone.jump", 2),
            Map.entry("stone.landing", 2),
            Map.entry("stone.run", 4),
            Map.entry("stone.sprint", 4),
            Map.entry("stone.walk", 4),
            Map.entry("water.jump", 2),
            Map.entry("water.landing", 2),
            Map.entry("water.run", 4),
            Map.entry("water.sprint", 4),
            Map.entry("water.walk", 4),
            Map.entry("wood.jump", 2),
            Map.entry("wood.landing", 2),
            Map.entry("wood.run", 4),
            Map.entry("wood.sprint", 4),
            Map.entry("wood.walk", 4),
            Map.entry("wood_thin.jump", 2),
            Map.entry("wood_thin.landing", 2),
            Map.entry("wood_thin.run", 4),
            Map.entry("wood_thin.sprint", 4),
            Map.entry("wood_thin.walk", 4)
    );


    private static final double STEP_SNEAK = 1.20;
    private static final double STEP_WALK = 1.55;
    private static final double STEP_SPRINT = 1.55;

    // sounds.json defines attenuation independently of the playback gain.
    private static final float GAIN_SNEAK = 0.5625f;
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
            if (!inTeam(p) || p.isSpectator() || p.isDeadOrDying()) {
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
        Vec3 vel = p.getDeltaMovement();

        if (st.last == null) {
            st.last = pos;
            st.wasOnGround = onGround;
            st.lastGroundY = pos.y;
            return;
        }

        if (st.wasOnGround && !onGround && vel.y > 0.15 && now - st.lastJumpTick > 8) {
            st.lastJumpTick = now;
            play(p, level, st, "jump", GAIN_JUMP, 0.02);
        }
        if (!onGround) {
            st.airFall = Math.max(st.airFall, p.fallDistance);
            st.accum = 0.0;
        } else if (!st.wasOnGround) {
            boolean steppedUp = !Double.isNaN(st.lastGroundY) && pos.y > st.lastGroundY + 0.4;
            if (st.airFall > 0.7 || steppedUp) {
                play(p, level, st, "landing", 1.0f, 0.0);
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
        double stepLen;
        float gain = 1.0f;
        if (p.isShiftKeyDown()) {
            state = "walk";
            stepLen = STEP_SNEAK;
            gain = GAIN_SNEAK;
        } else if (p.isSprinting()) {
            state = "sprint";
            stepLen = STEP_SPRINT;
        } else {
            state = "run";
            stepLen = STEP_WALK;
        }
        if (onGround && moved > 0.001 && moved < 1.5) {
            if (st.accum == 0.0 && moved > 0.02 && now - st.lastPlayedTick > 6) {
                st.accum = stepLen * 0.55;
            }
            st.accum += moved;
        }
        if (st.accum >= stepLen && now - st.lastPlayedTick >= 2) {
            st.accum = 0.0;
            st.lastPlayedTick = now;
            play(p, level, st, state, gain, 0.03);
        }
        st.last = pos;
        st.wasOnGround = onGround;
    }

    private static void play(Player p, ClientLevel level, State st, String state, float gain, double pitchJitter) {
        String material = materialOf(level, p.getOnPos());
        String variantKey = material + "." + state;
        int variants = Math.max(1, VARIANTS.getOrDefault(variantKey, 4));
        int variant = 1 + level.getRandom().nextInt(variants);
        if (variants > 1) {
            Integer last = st.lastVariant.get(variantKey);
            if (last != null && last == variant) {
                variant = variant % variants + 1;
            }
            st.lastVariant.put(variantKey, variant);
        }
        String id = "step." + material + "." + state + "." + variant;
        SoundEvent sound = SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(Cs2Footsteps.MODID, id));
        Vec3 vel = p.getDeltaMovement();
        double x = p.getX() + vel.x * 0.18;
        double z = p.getZ() + vel.z * 0.18;
        double y = p.getY() + 0.05;
        float pitch = (float) (1.0 + (level.getRandom().nextDouble() - 0.5) * pitchJitter * 2);
        level.playLocalSound(x, y, z, sound, SoundSource.PLAYERS, gain, pitch, false);
    }

    private static boolean inTeam(Player p) {
        if (p.getTeam() == null) {
            return false;
        }
        String name = p.getTeam().getName();
        return "cs2_t".equals(name) || "cs2_ct".equals(name);
    }

    private static String materialOf(ClientLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        SoundType sound = state.getSoundType();
        String path = block.builtInRegistryHolder().key().location().getPath();
        if (sound == SoundType.METAL) {
            return (path.contains("bars") || path.contains("chain") || path.contains("grate") || path.contains("rail"))
                    ? "metal_thin" : "metal";
        }
        if (sound == SoundType.WOOD) {
            return (path.contains("trapdoor") || path.contains("ladder") || path.contains("door"))
                    ? "wood_thin" : "wood";
        }
        if (sound == SoundType.GRAVEL) {
            return "gravel";
        }
        if (sound == SoundType.GRASS) {
            return "grass";
        }
        if (sound == SoundType.SAND) {
            return "dirt";
        }
        if (sound == SoundType.GLASS) {
            return "glass";
        }
        if (sound == SoundType.WOOL) {
            return "fabric";
        }
        if (sound == SoundType.SNOW) {
            return "snow";
        }
        return "stone";
    }
}
