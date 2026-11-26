package com.sandrone.cs2fairplay;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

import java.util.Set;

@Mod(Cs2FairPlay.MOD_ID)
public final class Cs2FairPlay {
    public static final String MOD_ID = "cs2fairplay";
    private static final Set<String> MAP_MODS = Set.of(
            "xaerominimap", "xaeroworldmap", "xaeroplus", "journeymap", "voxelmap", "map_atlases", "antiqueatlas");

    public Cs2FairPlay(IEventBus modBus) {
        modBus.addListener(this::registerPayloads);
        NeoForge.EVENT_BUS.register(MatchFairPlay.class);
        NeoForge.EVENT_BUS.register(MatchAudit.class);
        NeoForge.EVENT_BUS.register(InputAuditServer.class);
        TaCZAuditEvents.register();
        if (FMLEnvironment.dist == Dist.CLIENT) {
            FairPlayClient.register();
            NeoForge.EVENT_BUS.register(InputAuditClient.class);
        }
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        // With a known map mod installed the client declines the competitive channel.
        if (FMLEnvironment.dist == Dist.CLIENT && MAP_MODS.stream().anyMatch(ModList.get()::isLoaded)) {
            return;
        }
        var protocol = event.registrar("3").optional();
        AuditWire.register(event.registrar("1").optional());
        protocol.playToClient(Policy.TYPE, Policy.CODEC, (payload, context) ->
                FairPlayClient.applyPolicy(payload.firstPerson(), payload.watchingTeam()));
        protocol.playToServer(CycleTarget.TYPE, CycleTarget.CODEC, (payload, context) -> {
            if (context.player() instanceof ServerPlayer player && ready(player)) {
                MatchFairPlay.watch(player, payload.previous() ? -1 : 1);
            }
        });
    }

    static boolean ready(ServerPlayer player) {
        return player.connection != null && NetworkRegistry.hasChannel(player.connection, Policy.TYPE.id());
    }

    public record Policy(boolean firstPerson, boolean watchingTeam) implements CustomPacketPayload {
        static final Type<Policy> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "policy_v3"));
        static final StreamCodec<io.netty.buffer.ByteBuf, Policy> CODEC =
                StreamCodec.composite(ByteBufCodecs.BOOL, Policy::firstPerson, ByteBufCodecs.BOOL,
                        Policy::watchingTeam, Policy::new);

        @Override
        public Type<Policy> type() {
            return TYPE;
        }
    }

    public record CycleTarget(boolean previous) implements CustomPacketPayload {
        static final Type<CycleTarget> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MOD_ID, "cycle_target_v3"));
        static final StreamCodec<io.netty.buffer.ByteBuf, CycleTarget> CODEC =
                ByteBufCodecs.BOOL.map(CycleTarget::new, CycleTarget::previous);

        @Override
        public Type<CycleTarget> type() {
            return TYPE;
        }
    }
}
