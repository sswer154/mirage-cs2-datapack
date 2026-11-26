package com.sandrone.cs2fairplay;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.function.Consumer;

public final class FootstepWire {
    private static boolean localCustom;
    private static Consumer<Cue> playback = cue -> {};

    private FootstepWire() {}

    static void register(PayloadRegistrar registrar) {
        registrar.playToServer(Request.TYPE, Request.CODEC, (message, context) -> {
            if (context.player() instanceof ServerPlayer player) ServerFootsteps.request(player, message.motion());
        });
        registrar.playToClient(Policy.TYPE, Policy.CODEC, (message, context) -> localCustom = message.custom());
        registrar.playToClient(Cue.TYPE, Cue.CODEC, (message, context) -> playback.accept(message));
    }

    public static boolean localCustom() { return localCustom; }

    public static void resetLocal() { localCustom = false; }

    public static void setPlayback(Consumer<Cue> listener) { playback = listener; }

    public static final int STEP = 0;
    public static final int JUMP = 1;
    public static final int LANDING = 2;
    public static final int SNEAK = 3;
    public static final int SPRINT = 4;

    private static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> id(String name) {
        return new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Cs2FairPlay.MOD_ID, name));
    }

    public record Request(int motion) implements CustomPacketPayload {
        public static final Type<Request> TYPE = id("footstep_request_v1");
        static final StreamCodec<ByteBuf, Request> CODEC = ByteBufCodecs.VAR_INT.map(Request::new, Request::motion);
        @Override public Type<Request> type() { return TYPE; }
    }

    record Policy(boolean custom) implements CustomPacketPayload {
        static final Type<Policy> TYPE = id("footstep_policy_v1");
        static final StreamCodec<ByteBuf, Policy> CODEC = ByteBufCodecs.BOOL.map(Policy::new, Policy::custom);
        @Override public Type<Policy> type() { return TYPE; }
    }

    public record Cue(int source, int motion, BlockPos support, double x, double y, double z) implements CustomPacketPayload {
        static final Type<Cue> TYPE = id("footstep_cue_v1");
        static final StreamCodec<ByteBuf, Cue> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Cue::source, ByteBufCodecs.VAR_INT, Cue::motion, BlockPos.STREAM_CODEC, Cue::support,
                ByteBufCodecs.DOUBLE, Cue::x, ByteBufCodecs.DOUBLE, Cue::y, ByteBufCodecs.DOUBLE, Cue::z, Cue::new);
        @Override public Type<Cue> type() { return TYPE; }
    }
}
