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
    }

    private static final Map<UUID, State> STATES = new HashMap<>();

    /** 不同状态的步距（格）与传播距离（格）。 */
    private static final double STEP_SNEAK = 1.55;
    private static final double STEP_WALK = 1.00;
    private static final double STEP_SPRINT = 1.30;

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
        if (player.isSpectator() || player.isDeadOrDying() || !inMatch(player)) {
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
        if (st.wasOnGround && !onGround && player.getDeltaMovement().y > 0.18) {
            play(player, "jump", RANGE_JUMP, 0.02);
        }
        if (!onGround) {
            st.airFall = Math.max(st.airFall, player.fallDistance);
        } else if (!st.wasOnGround) {
            if (st.airFall > 0.7) {
                play(player, "landing", RANGE_LANDING, 0.0);
            }
            st.airFall = 0.0;
        }

        // 地面移动距离累计
        double dx = pos.x - st.last.x;
        double dz = pos.z - st.last.z;
        double moved = Math.sqrt(dx * dx + dz * dz);
        if (moved > 0.001 && moved < 1.5) {
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

    private static void play(ServerPlayer source, String state, double range, double pitchJitter) {
        ServerLevel level = source.serverLevel();
        String material = materialOf(level, source.blockPosition().below());
        String id = "step." + material + "." + state;
        Vec3 at = source.position();
        float volume = (float) (range / 16.0);
        float pitch = (float) (1.0 + (level.getRandom().nextDouble() - 0.5) * pitchJitter * 2);
        for (ServerPlayer listener : level.players()) {
            if (listener == source) {
                continue;
            }
            if (!Cs2Grenade.isMatchAudioListener(listener)) {
                continue;
            }
            if (listener.position().distanceToSqr(at) > (range + 2) * (range + 2)) {
                continue;
            }
            GrenadeManager.playCs2For(listener, level, at, id, volume, pitch);
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

    private static boolean inMatch(ServerPlayer player) {
        var sb = player.getScoreboard();
        var obj = sb.getObjective("cs2_inmatch");
        if (obj == null) {
            return false;
        }
        var info = sb.getPlayerScoreInfo(
                net.minecraft.world.scores.ScoreHolder.forNameOnly(player.getScoreboardName()), obj);
        return info != null && info.value() == 1;
    }
}
