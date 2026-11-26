package com.sandrone.cs2grenade;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 塔科夫风格脚步声：服务端按玩家移动距离触发，选材质与动作，逐个发声音包。
 * 原版脚步声不做处理，近处会有叠加；后续可以再用资源包静音原版。
 */
public final class FootstepSystem {
    private FootstepSystem() {
    }

    private static final class State {
        Vec3 last = null;
        double accum = 0.0;
        boolean wasOnGround = true;
        double airFall = 0.0;
        int lastPlayedTick = -100;
        int lastJumpTick = -100;
        double lastGroundY = Double.NaN;
        final java.util.HashMap<String, Integer> lastVariant = new java.util.HashMap<>();
    }

    private static final Map<UUID, State> STATES = new HashMap<>();

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


    /** 不同状态的步距（格）与传播距离（格）。 */
    private static final double STEP_SNEAK = 1.20;
    private static final double STEP_WALK = 1.90;
    private static final double STEP_SPRINT = 1.55;

    private static final double RANGE_SNEAK = 9.0;
    private static final double RANGE_WALK = 20.0;
    private static final double RANGE_SPRINT = 26.0;
    private static final double RANGE_LANDING = 22.0;
    private static final double RANGE_JUMP = 12.0;

    static void onLogout(ServerPlayer player) {
        STATES.remove(player.getUUID());
    }

    static void clear(ServerPlayer player) {
        STATES.remove(player.getUUID());
    }

    static void tick(ServerPlayer player, int now) {
        // 队伍 + 存活 + 非旁观；不看比赛阶段
        if (!inTeam(player) || player.isSpectator() || player.isDeadOrDying() || !isAlive(player)) {
            STATES.remove(player.getUUID());
            return;
        }
        State st = STATES.computeIfAbsent(player.getUUID(), k -> new State());
        Vec3 pos = player.position();
        boolean onGround = player.onGround();
        if (st.last == null) {
            st.last = pos;
            st.wasOnGround = onGround;
            return;
        }

        // 起跳 / 落地
        if (st.wasOnGround && !onGround && player.getDeltaMovement().y > 0.15
                && now - st.lastJumpTick > 8) {
            st.lastJumpTick = now;
            play(player, "jump", RANGE_JUMP, 0.02);
        }
        if (!onGround) {
            st.airFall = Math.max(st.airFall, player.fallDistance);
        } else if (!st.wasOnGround) {
            // 正常落地，或者跳上高一格时也算一次落地
            boolean steppedUp = !Double.isNaN(st.lastGroundY) && pos.y > st.lastGroundY + 0.4;
            if (st.airFall > 0.7 || steppedUp) {
                play(player, "landing", RANGE_LANDING, 0.0);
            }
            st.airFall = 0.0;
        }
        if (onGround) {
            st.lastGroundY = pos.y;
        }

        // 地面移动距离累计
        double dx = pos.x - st.last.x;
        double dz = pos.z - st.last.z;
        double moved = Math.sqrt(dx * dx + dz * dz);
        if (!onGround) {
            // 空中不累计步距，落地后重新开始
            st.accum = 0.0;
        } else if (moved > 0.001 && moved < 1.5) {
            if (st.accum == 0.0 && moved > 0.02 && now - st.lastPlayedTick > 6) {
                // 起步第一脚：不等走满一个步距
                st.accum = stepLenFor(player) * 0.55;
            }
            st.accum += moved;
        }

        String mode;
        double stepLen;
        double range;
        String soundState;
        if (player.isShiftKeyDown()) {
            mode = "sneak";
            stepLen = STEP_SNEAK;
            range = RANGE_SNEAK;
            soundState = "walk";
        } else if (player.isSprinting()) {
            mode = "sprint";
            stepLen = STEP_SPRINT;
            range = RANGE_SPRINT;
            soundState = "sprint";
        } else {
            mode = "walk";
            stepLen = STEP_WALK;
            range = RANGE_WALK;
            soundState = "run";
        }

        if (st.accum >= stepLen && now - st.lastPlayedTick >= 3) {
            st.accum = 0.0;
            st.lastPlayedTick = now;
            play(player, soundState, range, 0.03);
        }
        st.last = pos;
        st.wasOnGround = onGround;
    }

    private static double stepLenFor(ServerPlayer player) {
        if (player.isShiftKeyDown()) return STEP_SNEAK;
        return player.isSprinting() ? STEP_SPRINT : STEP_WALK;
    }

    private static void play(ServerPlayer source, String state, double range, double pitchJitter) {
        ServerLevel level = source.serverLevel();
        String material = materialOf(level, source.blockPosition().below());
        State st = STATES.computeIfAbsent(source.getUUID(), k -> new State());
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
        // 服务端一 tick 的滞后 + 采样前摇，往前补一点，听感更贴脚
        Vec3 vel = source.getDeltaMovement();
        Vec3 ahead = source.position().add(vel.x * 0.18, 0.0, vel.z * 0.18);
        Vec3 at = new Vec3(ahead.x, source.getY() + 0.05, ahead.z);
        float volume = (float) (range / 16.0);
        float pitch = (float) (1.0 + (level.getRandom().nextDouble() - 0.5) * pitchJitter * 2);
        for (ServerPlayer listener : level.players()) {
            if (listener.position().distanceToSqr(at) > (range + 2) * (range + 2)) {
                continue;
            }
            GrenadeManager.playCs2For(listener, level, at, id, volume, pitch);
            Cs2Grenade.debug("footstep {} -> {} vol={}", id, listener.getScoreboardName(), volume);
        }
    }

    private static String materialOf(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        SoundType sound = state.getSoundType();
        String path = block.builtInRegistryHolder().key().location().getPath();
        if (sound == SoundType.METAL) {
            return (path.contains("bars") || path.contains("chain") || path.contains("grate")
                    || path.contains("rail")) ? "metal_thin" : "metal";
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

    /** 听众：T/CT 队员与解说，不依赖 cs2_inmatch。 */
    private static boolean canHear(ServerPlayer player) {
        var team = player.getTeam();
        if (team == null) return false;
        String name = team.getName();
        return "cs2_t".equals(name) || "cs2_ct".equals(name) || "cs2_cast".equals(name);
    }

    private static boolean isAlive(ServerPlayer player) {
        var sb = player.getScoreboard();
        var obj = sb.getObjective("cs2_alive");
        if (obj == null) return true;
        var info = sb.getPlayerScoreInfo(
                net.minecraft.world.scores.ScoreHolder.forNameOnly(player.getScoreboardName()), obj);
        return info != null && info.value() == 1;
    }

    /** T/CT 队员都会发出脚步声；解说（cs2_cast）不发，只负责听。 */
    private static boolean inTeam(ServerPlayer player) {
        var team = player.getTeam();
        if (team == null) {
            return false;
        }
        String name = team.getName();
        return "cs2_t".equals(name) || "cs2_ct".equals(name);
    }
}
