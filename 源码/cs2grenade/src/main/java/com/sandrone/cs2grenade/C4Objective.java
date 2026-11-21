package com.sandrone.cs2grenade;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

/** Owns the unplanted ground item and the T-only objective display. */
final class C4Objective {
    private static final ScoreHolder STATE = ScoreHolder.forNameOnly("#state");
    private static final ScoreHolder BOMB = ScoreHolder.forNameOnly("#bomb");
    private static final ScoreHolder EPOCH = ScoreHolder.forNameOnly("#epoch");
    private static final String BOMB_STATE = "cs2_bomb_state";
    private static final String CARRIER = "cs2_carrier";
    private static final String DROP_ROUND = "cs2grenade:c4_round";
    private static final String PICKUP_AT = "cs2grenade:c4_pickup_at";
    private static final String[] DIRECTIONS = {"↑", "↗", "→", "↘", "↓", "↙", "←", "↖"};

    private final Map<ServerPlayer, ServerBossEvent> displays = new HashMap<>();
    private ItemEntity dropped;

    void register(IEventBus bus) {
        bus.addListener(this::registerCommands);
        bus.addListener(EventPriority.LOWEST, this::onDeath);
        bus.addListener(EventPriority.LOWEST, this::onToss);
        bus.addListener(EventPriority.LOWEST, this::onItemJoin);
        bus.addListener(this::beforePickup);
        bus.addListener(this::afterPickup);
    }

    private void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("cs2c4")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("drop").executes(context ->
                        dropCarriedBomb(context.getSource().getPlayerOrException()) ? 1 : 0))
                .then(Commands.literal("give")
                        .then(Commands.argument("item", ItemArgument.item(event.getBuildContext()))
                                .executes(context -> giveBomb(context.getSource().getPlayerOrException(),
                                        ItemArgument.getItem(context, "item").createItemStack(1, true)) ? 1 : 0))));
    }

    private boolean giveBomb(ServerPlayer player, ItemStack stack) {
        Scoreboard board = player.getScoreboard();
        if (!isBomb(stack) || !isT(player) || phase(board) != 3 || score(board, BOMB, BOMB_STATE) != 0) {
            return false;
        }
        if (player.getInventory().add(stack)) {
            write(board, player, CARRIER, 1);
            write(board, BOMB, BOMB_STATE, 1);
            return true;
        }
        // Full inventory: place the original round C4 on the ground.
        return spawnDrop(player, stack);
    }

    private void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            dropCarriedBomb(player);
        }
    }

    void onLogout(ServerPlayer player) {
        dropCarriedBomb(player);
        ServerBossEvent display = displays.remove(player);
        if (display != null) {
            display.removePlayer(player);
        }
    }

    private boolean dropCarriedBomb(ServerPlayer player) {
        if (!isActiveCarrier(player)) {
            return false;
        }
        ItemStack carried = carriedBomb(player);
        if (carried.isEmpty()) {
            return false;
        }
        if (!spawnDrop(player, carried.copy())) {
            return false;
        }
        carried.setCount(0);
        player.getInventory().setChanged();
        return true;
    }

    private boolean spawnDrop(ServerPlayer player, ItemStack stack) {
        ServerLevel level = player.serverLevel();
        ItemEntity item = new ItemEntity(level, player.getX(), player.getY(), player.getZ(), stack);
        item.setDeltaMovement(Vec3.ZERO);
        prepareDrop(item, level);
        if (!level.addFreshEntity(item)) {
            return false;
        }
        markDropped(level.getScoreboard());
        return true;
    }

    private void onToss(ItemTossEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player && isActiveCarrier(player)
                && isBomb(event.getEntity().getItem())) {
            prepareDrop(event.getEntity(), player.serverLevel());
        }
    }

    private static void prepareDrop(ItemEntity item, ServerLevel level) {
        item.getPersistentData().putInt(DROP_ROUND, round(level.getScoreboard()));
        item.getPersistentData().putLong(PICKUP_AT, level.getGameTime() + 10);
        item.setNeverPickUp();
        item.setUnlimitedLifetime();
        item.setInvulnerable(true);
    }

    private void onItemJoin(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getEntity() instanceof ItemEntity item)
                || !isBomb(item.getItem()) || !item.getPersistentData().contains(DROP_ROUND)) {
            return;
        }
        Scoreboard board = level.getScoreboard();
        int state = score(board, BOMB, BOMB_STATE);
        boolean validState = event.loadedFromDisk() ? state == 2 : state == 0 || state == 1;
        if (phase(board) != 3 || !validState || !belongsToRound(item, board)
                || (usableDrop(board) && !dropped.getUUID().equals(item.getUUID()))) {
            event.setCanceled(true);
            return;
        }
        dropped = item;
    }

    private void beforePickup(ItemEntityPickupEvent.Pre event) {
        ItemEntity item = event.getItemEntity();
        if (!(event.getPlayer() instanceof ServerPlayer player) || !isBomb(item.getItem())) {
            return;
        }
        Scoreboard board = player.getScoreboard();
        boolean allowed = item == dropped && phase(board) == 3 && score(board, BOMB, BOMB_STATE) == 2
                && belongsToRound(item, board) && isT(player) && player.isAlive() && !player.isSpectator()
                && score(board, player, "cs2_alive") == 1 && carriedBomb(player).isEmpty()
                && player.serverLevel().getGameTime() >= item.getPersistentData().getLong(PICKUP_AT);
        event.setCanPickup(allowed ? TriState.TRUE : TriState.FALSE);
    }

    private void afterPickup(ItemEntityPickupEvent.Post event) {
        if (event.getItemEntity() == dropped && event.getPlayer() instanceof ServerPlayer player
                && isBomb(event.getOriginalStack()) && dropped.getItem().isEmpty()) {
            Scoreboard board = player.getScoreboard();
            write(board, player, CARRIER, 1);
            write(board, BOMB, BOMB_STATE, 1);
            dropped = null;
            player.displayClientMessage(Component.literal("已拾取 C4"), true);
        }
    }

    void tick(MinecraftServer server, boolean live, boolean planted) {
        Scoreboard board = server.getScoreboard();
        Entity target = null;
        if (planted) {
            target = C4Sfx.findBomb(server);
        } else if (live) {
            int bombState = score(board, BOMB, BOMB_STATE);
            if (usableDrop(board)) {
                if (bombState == 1 && !dropped.isRemoved()) {
                    // A manual toss reaches the world after ItemTossEvent returns.
                    markDropped(board);
                    bombState = 2;
                }
                if (bombState == 2) {
                    target = dropped;
                }
            } else {
                dropped = null;
            }
            if (bombState == 1) {
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    if (isActiveCarrier(player) && !carriedBomb(player).isEmpty()) {
                        target = player;
                        break;
                    }
                }
            }
        }
        if (!live || planted) {
            dropped = null;
        }

        Entity objective = target;
        displays.entrySet().removeIf(entry -> {
            if (objective != null && isT(entry.getKey())) {
                return false;
            }
            entry.getValue().removePlayer(entry.getKey());
            return true;
        });
        if (target != null) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (!isT(player)) {
                    continue;
                }
                Component label = label(player, target, planted);
                ServerBossEvent display = displays.get(player);
                if (display == null) {
                    display = new ServerBossEvent(label, BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);
                    displays.put(player, display);
                    display.addPlayer(player);
                } else {
                    display.setName(label);
                }
            }
        }
    }

    private static Component label(ServerPlayer player, Entity target, boolean planted) {
        if (player == target) {
            return Component.literal("C4 · 你持有");
        }
        String status = planted ? "已安放" : target instanceof ItemEntity ? "已掉落" : "队友携带";
        var pos = target.blockPosition();
        String coordinates = pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
        if (player.level() != target.level()) {
            return Component.literal("C4 · " + status + " · " + target.level().dimension().location() + " · " + coordinates);
        }
        Vec3 delta = target.position().subtract(player.position());
        double yaw = Math.toDegrees(Math.atan2(-delta.x, delta.z));
        int octant = Math.floorMod((int) Math.round(Mth.wrapDegrees(yaw - player.getYRot()) / 45), 8);
        String direction = delta.horizontalDistanceSqr() < 1 ? "" : DIRECTIONS[octant] + " ";
        return Component.literal("C4 · " + status + " · " + direction + Math.round(delta.length()) + " m · " + coordinates);
    }

    private boolean usableDrop(Scoreboard board) {
        return dropped != null && belongsToRound(dropped, board)
                && (dropped.getRemovalReason() == Entity.RemovalReason.UNLOADED_TO_CHUNK
                || (!dropped.isRemoved() && ((ServerLevel) dropped.level()).getEntity(dropped.getUUID()) == dropped));
    }

    private static boolean belongsToRound(ItemEntity item, Scoreboard board) {
        return item.getPersistentData().contains(DROP_ROUND)
                && item.getPersistentData().getInt(DROP_ROUND) == round(board);
    }

    private static void markDropped(Scoreboard board) {
        var objective = board.getObjective(CARRIER);
        if (objective != null) {
            for (ScoreHolder holder : board.getTrackedPlayers()) {
                board.resetSinglePlayerScore(holder, objective);
            }
        }
        write(board, BOMB, BOMB_STATE, 2);
    }

    private static boolean isActiveCarrier(ServerPlayer player) {
        Scoreboard board = player.getScoreboard();
        return phase(board) == 3 && score(board, BOMB, BOMB_STATE) == 1
                && isT(player) && score(board, player, CARRIER) == 1;
    }

    private static boolean isT(ServerPlayer player) {
        return player.getTeam() != null && "cs2_t".equals(player.getTeam().getName())
                && score(player.getScoreboard(), player, "cs2_inmatch") == 1;
    }

    private static ItemStack carriedBomb(ServerPlayer player) {
        for (var compartment : List.of(player.getInventory().items, player.getInventory().offhand)) {
            for (ItemStack stack : compartment) {
                if (isBomb(stack)) {
                    return stack;
                }
            }
        }
        ItemStack cursor = player.containerMenu.getCarried();
        return isBomb(cursor) ? cursor : ItemStack.EMPTY;
    }

    static boolean isBomb(ItemStack stack) {
        var data = stack.get(DataComponents.CUSTOM_DATA);
        return stack.is(Items.NAUTILUS_SHELL) && data != null && data.copyTag().getBoolean("cs2_bomb");
    }

    private static int phase(Scoreboard board) {
        return score(board, STATE, "cs2_state");
    }

    private static int round(Scoreboard board) {
        return score(board, EPOCH, "cs2_health_round");
    }

    private static int score(Scoreboard board, ScoreHolder holder, String name) {
        var objective = board.getObjective(name);
        var value = objective == null ? null : board.getPlayerScoreInfo(holder, objective);
        return value == null ? 0 : value.value();
    }

    private static void write(Scoreboard board, ScoreHolder holder, String name, int value) {
        var objective = board.getObjective(name);
        if (objective != null) {
            board.getOrCreatePlayerScore(holder, objective).set(value);
        }
    }
}
