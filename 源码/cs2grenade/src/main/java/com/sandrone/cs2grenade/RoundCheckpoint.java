package com.sandrone.cs2grenade;

import java.util.List;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** Captures the loadout and economy before players can buy for the round. */
final class RoundCheckpoint {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Cs2Grenade.MODID);
    private static final DeferredHolder<AttachmentType<?>, AttachmentType<CompoundTag>> CHECKPOINT =
            ATTACHMENTS.register("round_checkpoint", () -> AttachmentType.builder(() -> new CompoundTag())
                    .serialize(CompoundTag.CODEC).copyOnDeath().build());
    private static final ScoreHolder BASELINE = ScoreHolder.forNameOnly("#checkpoint");
    private static final List<String> SCORES = List.of(
            "cs2_money", "cs2_primary", "cs2_secondary", "cs2_armor", "cs2_kit", "cs2_wclass",
            "cs2_kills", "cs2_deaths");

    private RoundCheckpoint() {
    }

    static void register(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
        NeoForge.EVENT_BUS.addListener(RoundCheckpoint::registerCommands);
    }

    private static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("cs2roundstate")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("save").executes(context -> save(context.getSource().getPlayerOrException())))
                .then(Commands.literal("available").executes(context ->
                        decode(context.getSource().getPlayerOrException()) != null ? 1 : 0))
                .then(Commands.literal("restore").executes(context -> restore(context.getSource().getPlayerOrException())))
                .then(Commands.literal("discard").executes(context -> {
                    context.getSource().getPlayerOrException().removeData(CHECKPOINT);
                    return 1;
                })));
    }

    private static int save(ServerPlayer player) {
        Scoreboard board = player.getScoreboard();
        ScoreHolder holder = ScoreHolder.forNameOnly(player.getScoreboardName());
        int baseline = score(board, BASELINE, "cs2_health_round");
        int side = score(board, holder, "cs2_team");
        if (baseline <= 0 || side < 1 || side > 2) {
            return 0;
        }
        if (checkpoint(player, baseline, side) != null) {
            return 1;
        }
        player.closeContainer();
        CompoundTag snapshot = new CompoundTag();
        snapshot.putInt("Baseline", baseline);
        snapshot.putInt("Team", side);
        snapshot.putInt("Selected", player.getInventory().selected);
        snapshot.put("Items", player.getInventory().save(new ListTag()));
        CompoundTag values = new CompoundTag();
        for (String name : SCORES) {
            values.putInt(name, score(board, holder, name));
        }
        snapshot.put("Scores", values);
        player.setData(CHECKPOINT, snapshot);
        return 1;
    }

    static void creditBaseline(ServerPlayer player, int amount) {
        Scoreboard board = player.getScoreboard();
        ScoreHolder holder = ScoreHolder.forNameOnly(player.getScoreboardName());
        CompoundTag snapshot = checkpoint(player, score(board, BASELINE, "cs2_health_round"),
                score(board, holder, "cs2_team"));
        if (snapshot != null && snapshot.get("Scores") instanceof CompoundTag values
                && values.contains("cs2_money")) {
            // A settlement award belongs to the opening balance, even if it was claimed after the save.
            values.putInt("cs2_money", values.getInt("cs2_money") + amount);
            player.setData(CHECKPOINT, snapshot);
        }
    }

    private static CompoundTag checkpoint(ServerPlayer player, int baseline, int side) {
        if (!player.hasData(CHECKPOINT) || baseline <= 0) {
            return null;
        }
        CompoundTag snapshot = player.getData(CHECKPOINT);
        return snapshot.getInt("Baseline") == baseline && snapshot.getInt("Team") == side ? snapshot : null;
    }

    private record Decoded(Inventory inventory, CompoundTag scores) {
    }

    private static Decoded decode(ServerPlayer player) {
        Scoreboard board = player.getScoreboard();
        ScoreHolder holder = ScoreHolder.forNameOnly(player.getScoreboardName());
        CompoundTag snapshot = checkpoint(player, score(board, BASELINE, "cs2_health_round"),
                score(board, holder, "cs2_team"));
        if (snapshot == null || !(snapshot.get("Items") instanceof ListTag items)
                || !(snapshot.get("Scores") instanceof CompoundTag values)
                || !values.contains("cs2_kills") || !values.contains("cs2_deaths")) {
            return null;
        }
        Inventory restored = new Inventory(player);
        restored.load(items);
        int stacks = 0;
        for (int slot = 0; slot < restored.getContainerSize(); slot++) {
            if (!restored.getItem(slot).isEmpty()) {
                stacks++;
            }
        }
        if (stacks != items.size()) {
            return null;
        }
        restored.selected = snapshot.getInt("Selected");
        return new Decoded(restored, values);
    }

    private static int restore(ServerPlayer player) {
        Decoded checkpoint = decode(player);
        if (checkpoint == null) {
            return 0;
        }
        player.closeContainer();
        player.getInventory().replaceWith(checkpoint.inventory());
        player.getInventory().setChanged();
        Scoreboard board = player.getScoreboard();
        ScoreHolder holder = ScoreHolder.forNameOnly(player.getScoreboardName());
        for (String name : SCORES) {
            var objective = board.getObjective(name);
            if (objective != null) {
                board.getOrCreatePlayerScore(holder, objective).set(checkpoint.scores().getInt(name));
            }
        }
        var previousDeaths = board.getObjective("cs2_prev_deaths");
        if (previousDeaths != null) {
            board.getOrCreatePlayerScore(holder, previousDeaths).set(checkpoint.scores().getInt("cs2_deaths"));
        }
        player.inventoryMenu.broadcastFullState();
        player.connection.send(new ClientboundSetCarriedItemPacket(player.getInventory().selected));
        return 1;
    }

    private static int score(Scoreboard board, ScoreHolder holder, String name) {
        var objective = board.getObjective(name);
        if (objective == null) {
            return 0;
        }
        var info = board.getPlayerScoreInfo(holder, objective);
        return info == null ? 0 : info.value();
    }
}
