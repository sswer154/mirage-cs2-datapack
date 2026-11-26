package com.sandrone.cs2fairplay;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.UUID;

final class AuditWire {
    static final int CHUNK = 8192;
    static final int MAX_FILE = 2 * 1024 * 1024;

    static void register(PayloadRegistrar protocol) {
        protocol.playToClient(Clock.TYPE, Clock.CODEC, (msg, ctx) -> InputAuditClient.clock(msg));
        protocol.playToClient(Finish.TYPE, Finish.CODEC, (msg, ctx) -> InputAuditClient.finish(msg));
        protocol.playToClient(Ack.TYPE, Ack.CODEC, (msg, ctx) -> InputAuditClient.ack(msg));
        protocol.playToServer(Part.TYPE, Part.CODEC, (msg, ctx) -> {
            if (ctx.player() instanceof ServerPlayer player) InputAuditServer.upload(player, msg);
        });
    }

    private static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> type(String name) {
        return new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Cs2FairPlay.MOD_ID, name));
    }

    static boolean validName(String name) {
        return name.matches("[0-9]{8}-[0-9]{4}-场次[0-9]{3,7}");
    }

    record Clock(UUID match, String name, int tick, int round, boolean upload) implements CustomPacketPayload {
        static final Type<Clock> TYPE = AuditWire.type("audit_clock_v1");
        static final StreamCodec<ByteBuf, Clock> CODEC = StreamCodec.composite(UUIDUtil.STREAM_CODEC, Clock::match,
                ByteBufCodecs.stringUtf8(40), Clock::name, ByteBufCodecs.VAR_INT, Clock::tick, ByteBufCodecs.VAR_INT, Clock::round,
                ByteBufCodecs.BOOL, Clock::upload, Clock::new);
        public Type<Clock> type() { return TYPE; }
    }

    record Finish(UUID match, String name, boolean upload) implements CustomPacketPayload {
        static final Type<Finish> TYPE = AuditWire.type("audit_finish_v1");
        static final StreamCodec<ByteBuf, Finish> CODEC = StreamCodec.composite(UUIDUtil.STREAM_CODEC, Finish::match,
                ByteBufCodecs.stringUtf8(40), Finish::name, ByteBufCodecs.BOOL, Finish::upload, Finish::new);
        public Type<Finish> type() { return TYPE; }
    }

    record Part(UUID match, int offset, int total, byte[] bytes) implements CustomPacketPayload {
        static final Type<Part> TYPE = AuditWire.type("audit_part_v1");
        static final StreamCodec<ByteBuf, Part> CODEC = StreamCodec.composite(UUIDUtil.STREAM_CODEC, Part::match,
                ByteBufCodecs.VAR_INT, Part::offset, ByteBufCodecs.VAR_INT, Part::total,
                ByteBufCodecs.byteArray(CHUNK), Part::bytes, Part::new);
        public Type<Part> type() { return TYPE; }
    }

    record Ack(UUID match) implements CustomPacketPayload {
        static final Type<Ack> TYPE = AuditWire.type("audit_ack_v1");
        static final StreamCodec<ByteBuf, Ack> CODEC = UUIDUtil.STREAM_CODEC.map(Ack::new, Ack::match);
        public Type<Ack> type() { return TYPE; }
    }
}
