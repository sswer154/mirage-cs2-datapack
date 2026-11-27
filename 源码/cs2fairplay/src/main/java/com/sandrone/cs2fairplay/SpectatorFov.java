package com.sandrone.cs2fairplay;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.IdentityHashMap;
import java.util.Map;

final class SpectatorFov {
    private static final Map<ServerPlayer, Sample> SAMPLES = new IdentityHashMap<>();
    private static final Map<ServerPlayer, Observed> SENT = new IdentityHashMap<>();

    private SpectatorFov() {}

    static void register(PayloadRegistrar protocol) {
        protocol.playToServer(Sample.TYPE, Sample.CODEC, (sample, context) -> {
            if (context.player() instanceof ServerPlayer player && Cs2FairPlay.ready(player)
                    && MatchFairPlay.formal(player.getServer()) && MatchFairPlay.participant(player)
                    && !player.isSpectator() && sample.valid()) SAMPLES.put(player, sample);
        });
        protocol.playToClient(Observed.TYPE, Observed.CODEC, (sample, context) -> FairPlayClient.acceptFov(sample));
    }

    static void update(ServerPlayer viewer, ServerPlayer target) {
        var sample = SAMPLES.get(target);
        if (sample == null) {
            SENT.remove(viewer);
            return;
        }
        var observed = new Observed(target.getId(), sample);
        if (!observed.equals(SENT.put(viewer, observed))) PacketDistributor.sendToPlayer(viewer, observed);
    }

    static void forget(ServerPlayer player) {
        SAMPLES.remove(player);
        SENT.remove(player);
    }

    static void stop() {
        SAMPLES.clear();
        SENT.clear();
    }

    record Sample(float world, float hands, float aspect) implements CustomPacketPayload {
        static final Type<Sample> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Cs2FairPlay.MOD_ID, "view_sample"));
        static final StreamCodec<ByteBuf, Sample> CODEC = StreamCodec.composite(
                ByteBufCodecs.FLOAT, Sample::world, ByteBufCodecs.FLOAT, Sample::hands,
                ByteBufCodecs.FLOAT, Sample::aspect, Sample::new);

        boolean valid() {
            return world >= 1 && world <= 175 && hands >= 1 && hands <= 175 && aspect >= 0.25f && aspect <= 8;
        }

        double worldForAspect(float viewerAspect) {
            if (viewerAspect <= aspect) return world;
            double halfAngle = Math.tan(Math.toRadians(world) / 2);
            return Math.toDegrees(2 * Math.atan(halfAngle * aspect / viewerAspect));
        }

        @Override public Type<Sample> type() { return TYPE; }
    }

    record Observed(int target, Sample fov) implements CustomPacketPayload {
        static final Type<Observed> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Cs2FairPlay.MOD_ID, "observed_view"));
        static final StreamCodec<ByteBuf, Observed> CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Observed::target, Sample.CODEC, Observed::fov, Observed::new);

        @Override public Type<Observed> type() { return TYPE; }
    }
}
