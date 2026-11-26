package com.sandrone.cs2grenade;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.scores.ScoreHolder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

final class MatchSpawns {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Cs2Grenade.MODID);
    private static final DeferredHolder<AttachmentType<?>, AttachmentType<CompoundTag>> SAVED =
            ATTACHMENTS.register("match_spawn", () -> AttachmentType.builder(() -> new CompoundTag())
                    .serialize(CompoundTag.CODEC).copyOnDeath().build());
    private static final ResourceLocation MAP = ResourceLocation.fromNamespaceAndPath("cs2", "map");

    static void register(IEventBus bus) {
        ATTACHMENTS.register(bus);
        NeoForge.EVENT_BUS.addListener(MatchSpawns::registerCommands);
    }

    private static void registerCommands(RegisterCommandsEvent event) {
        var root = Commands.literal("cs2spawn").requires(source -> source.hasPermission(2))
                .then(Commands.literal("teleport").executes(ctx -> teleport(ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("restore").executes(ctx -> restore(ctx.getSource().getPlayerOrException())));
        for (String side : List.of("t", "ct")) {
            root.then(Commands.literal("add_" + side).executes(ctx -> configure(ctx.getSource(), side, true)))
                .then(Commands.literal("remove_" + side).executes(ctx -> configure(ctx.getSource(), side, false)));
        }
        event.getDispatcher().register(root);
    }

    private static int teleport(ServerPlayer player) {
        int side = score(player, player, "cs2_team");
        if (side != 1 && side != 2 || score(player, player, "cs2_inmatch") != 1) return 0;
        String tag = "cs2_spawn_" + (side == 1 ? "t" : "ct");
        var markers = markers(player,tag);
        if (markers.isEmpty()) return 0;
        CompoundTag state = player.getData(SAVED);
        if (!state.contains("Dimension")) {
            state.putString("Dimension", player.getRespawnDimension().location().toString());
            if (player.getRespawnPosition() != null) state.putLong("Pos", player.getRespawnPosition().asLong());
            state.putFloat("Angle", player.getRespawnAngle());
            state.putBoolean("Forced", player.isRespawnForced());
        }
        int round = score(player, ScoreHolder.forNameOnly("#epoch"), "cs2_health_round");
        Marker target = null;
        if (state.getInt("Round") == round && state.hasUUID("Marker")) {
            var saved = state.getUUID("Marker");
            target = markers.stream().filter(marker -> marker.getUUID().equals(saved)).findFirst().orElse(null);
        }
        if (target == null) {
            int fewest = markers.stream().mapToInt(marker -> uses(marker, round)).min().orElseThrow();
            var choices = markers.stream().filter(marker -> uses(marker, round) == fewest).toList();
            target = choices.get(player.serverLevel().getRandom().nextInt(choices.size()));
            target.getPersistentData().putInt("Cs2SpawnRound", round);
            target.getPersistentData().putInt("Cs2SpawnUses", fewest + 1);
            state.putInt("Round", round);
            state.putUUID("Marker", target.getUUID());
            player.setData(SAVED, state);
        }
        var level = (net.minecraft.server.level.ServerLevel) target.level();
        player.setRespawnPosition(level.dimension(), target.blockPosition(), target.getYRot(), true, false);
        player.teleportTo(level, target.getX(), target.getY(), target.getZ(), target.getYRot(), target.getXRot());
        return 1;
    }

    private static int restore(ServerPlayer player) {
        if (!player.hasData(SAVED)) return 0;
        var state = player.getData(SAVED);
        var id = ResourceLocation.tryParse(state.getString("Dimension"));
        if (id == null) return 0;
        var dimension = ResourceKey.create(Registries.DIMENSION, id);
        var pos = state.contains("Pos") ? BlockPos.of(state.getLong("Pos")) : null;
        player.setRespawnPosition(dimension, pos, state.getFloat("Angle"), state.getBoolean("Forced"), false);
        player.removeData(SAVED);
        return 1;
    }

    private static int uses(Marker marker, int round) {
        var data = marker.getPersistentData();
        return data.getInt("Cs2SpawnRound") == round ? data.getInt("Cs2SpawnUses") : 0;
    }

    private static List<Marker> markers(ServerPlayer player, String tag) {
        var found = new ArrayList<Marker>();
        for (var level : player.getServer().getAllLevels()) {
            found.addAll(level.getEntities(EntityType.MARKER, marker -> marker.getTags().contains(tag)));
        }
        return found;
    }

    private static int configure(CommandSourceStack source, String side, boolean add)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var player = source.getPlayerOrException();
        int phase = score(player, ScoreHolder.forNameOnly("#state"), "cs2_state");
        if (phase != 0 && phase != 7) {
            source.sendFailure(Component.literal("结束比赛后才能调整出生点。"));
            return 0;
        }
        String tag = "cs2_spawn_" + side;
        String active = source.getServer().getCommandStorage().get(MAP).getString("active");
        if (!active.matches("[a-z0-9_-]+")) {
            source.sendFailure(Component.literal("请先切换到已配置的命名地图。"));
            return 0;
        }
        String mapTag = "cs2_map_" + active + "_spawn_" + side;
        if (!add) {
            var target = markers(player, mapTag).stream().filter(marker -> marker.level() == player.level()
                    && marker.getTags().contains("cs2_extra_spawn") && marker.distanceToSqr(player) <= 4)
                    .min(java.util.Comparator.comparingDouble(marker -> marker.distanceToSqr(player)));
            if (target.isEmpty()) {
                source.sendFailure(Component.literal("两格内没有可移除的额外出生点。"));
                return 0;
            }
            target.orElseThrow().discard();
            source.sendSuccess(() -> Component.literal("已移除额外出生点。"), false);
            return 1;
        }
        var marker = new Marker(EntityType.MARKER, player.serverLevel());
        marker.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
        for (String value : List.of(tag, mapTag, "cs2_map_marker", "cs2_extra_spawn")) marker.addTag(value);
        if (!player.serverLevel().addFreshEntity(marker)) return 0;
        source.getServer().getCommands().performPrefixedCommand(source.withSuppressedOutput(), "cs2chunks retain ~ ~");
        source.sendSuccess(() -> Component.literal("已添加额外出生点。"), false);
        return 1;
    }

    private static int score(ServerPlayer player, ScoreHolder holder, String name) {
        var board = player.getScoreboard();
        var objective = board.getObjective(name);
        var score = objective == null ? null : board.getPlayerScoreInfo(holder, objective);
        return score == null ? 0 : score.value();
    }
}
