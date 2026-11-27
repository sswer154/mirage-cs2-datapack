package com.sandrone.cs2fairplay;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class FootstepWire {
    private record ClientPolicy(ResourceLocation dimension, Set<Integer> sources) {}
    private static ClientPolicy local;

    private FootstepWire() {}

    static void register(PayloadRegistrar registrar) {
        registrar.playToClient(Policy.TYPE, Policy.CODEC, (message, context) ->
                local = new ClientPolicy(message.dimension(), new HashSet<>(message.sources())));
    }

    public static boolean permits(ResourceLocation dimension, int source) {
        return local != null && local.dimension().equals(dimension) && local.sources().contains(source);
    }

    static void resetLocal() { local = null; }

    record Policy(ResourceLocation dimension, List<Integer> sources) implements CustomPacketPayload {
        static final Type<Policy> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Cs2FairPlay.MOD_ID, "footstep_policy_v2"));
        static final StreamCodec<ByteBuf, Policy> CODEC = StreamCodec.composite(
                ResourceLocation.STREAM_CODEC, Policy::dimension,
                ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), Policy::sources, Policy::new);
        @Override public Type<Policy> type() { return TYPE; }
    }
}
