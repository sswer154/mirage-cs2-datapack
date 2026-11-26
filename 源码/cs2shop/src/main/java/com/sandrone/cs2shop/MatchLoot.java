package com.sandrone.cs2shop;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.scores.ScoreHolder;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;

final class MatchLoot {
    private static final String LOOT_TAG = "cs2_match_loot";
    private static final String HANDOFF_TAG = "cs2_gun_handoff";
    private static final String LOOT_ROUND = "cs2:loot_round";
    private static final String DROPPED_ROUND = "cs2:loot_dropped_round";
    private static final ScoreHolder ROUND = ScoreHolder.forNameOnly("#epoch");
    private static final ScoreHolder STATE = ScoreHolder.forNameOnly("#state");

    private MatchLoot() {}

    static int dropOnDeath(ServerPlayer victim, boolean includeGrenade) {
        int phase = score(victim, STATE, "cs2_state");
        int round = score(victim, ROUND, "cs2_health_round");
        if (phase < 3 || phase > 4 || round < 1 || !isRoundParticipant(victim)
                || !victim.serverLevel().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)
                || victim.getPersistentData().getInt(DROPPED_ROUND) == round) {
            return 0;
        }
        victim.getPersistentData().putInt(DROPPED_ROUND, round);

        List<ItemStack> carried = carriedBy(victim);
        GunPurchase.Slot slot = GunPurchase.Slot.PRIMARY;
        ItemStack gun = findGun(carried, slot);
        if (gun.isEmpty()) {
            slot = GunPurchase.Slot.SECONDARY;
            gun = findGun(carried, slot);
        }

        int dropped = 0;
        if (!gun.isEmpty()) {
            ItemStack loot = gun.copyWithCount(1);
            slot.mark(loot);
            // Only guns bought by the current holder can be refunded by the shop.
            GunPurchase.removePurchasePrice(loot);
            if (drop(victim, gun, loot, round)) {
                setGunScore(victim, slot, !findGun(carried, slot).isEmpty());
                dropped++;
            }
        }

        if (includeGrenade && dropRandomGrenade(victim, carried, round)) {
            dropped++;
        }
        if (dropped != 0) {
            victim.getInventory().setChanged();
            victim.containerMenu.broadcastChanges();
        }
        return dropped;
    }

    private static boolean dropRandomGrenade(ServerPlayer victim, List<ItemStack> carried, int round) {
        var grenadeTypes = new LinkedHashMap<String, ItemStack>();
        for (ItemStack stack : carried) {
            var data = stack.get(DataComponents.CUSTOM_DATA);
            if (stack.is(Items.SNOWBALL) && data != null) {
                String type = data.copyTag().getString("cs2_util");
                if (!type.isEmpty()) {
                    grenadeTypes.putIfAbsent(type, stack);
                }
            }
        }
        if (grenadeTypes.isEmpty()) {
            return false;
        }
        List<ItemStack> options = List.copyOf(grenadeTypes.values());
        ItemStack grenade = options.get(victim.getRandom().nextInt(options.size()));
        return drop(victim, grenade, grenade.copyWithCount(1), round);
    }

    private static boolean drop(ServerPlayer victim, ItemStack source, ItemStack loot, int round) {
        var level = victim.serverLevel();
        var item = new ItemEntity(level, victim.getX(), victim.getY() + 0.25, victim.getZ(), loot);
        markRoundLoot(item, round);
        item.setDefaultPickUpDelay();
        if (!level.addFreshEntity(item)) {
            return false;
        }
        source.shrink(1);
        return true;
    }

    static void onToss(ItemTossEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player) || !isRoundParticipant(player)
                || !player.isAlive() || player.isSpectator() || score(player, STATE, "cs2_state") != 2
                || GunPurchase.slotOf(event.getEntity().getItem()) == null) {
            return;
        }
        int round = score(player, ROUND, "cs2_health_round");
        if (round > 0) {
            ItemEntity item = event.getEntity();
            markRoundLoot(item, round);
            item.addTag(HANDOFF_TAG);
        }
    }

    private static void markRoundLoot(ItemEntity item, int round) {
        item.addTag(LOOT_TAG);
        item.getPersistentData().putInt(LOOT_ROUND, round);
    }

    static void beforePickup(ItemEntityPickupEvent.Pre event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }
        ItemEntity item = event.getItemEntity();
        boolean handoff = item.getTags().contains(HANDOFF_TAG);
        boolean activePlayer = player.isAlive() && !player.isSpectator() && isRoundParticipant(player);
        if (item.getTags().contains(LOOT_TAG)) {
            int phase = score(player, STATE, "cs2_state");
            int round = score(player, ROUND, "cs2_health_round");
            if (phase < (handoff ? 2 : 3) || phase > 5 || round != item.getPersistentData().getInt(LOOT_ROUND)) {
                event.setCanPickup(TriState.FALSE);
                item.discard();
                return;
            }
            if (!activePlayer) {
                event.setCanPickup(TriState.FALSE);
                return;
            }
        }
        ItemStack stack = item.getItem();
        GunPurchase.Slot slot = GunPurchase.slotOf(stack);
        if (!activePlayer || slot == null) {
            return;
        }
        if (stack.getCount() != 1) {
            event.setCanPickup(TriState.FALSE);
            return;
        }
        if (!findGun(carriedBy(player), slot).isEmpty()) {
            event.setCanPickup(TriState.FALSE);
            if (handoff) {
                String category = slot == GunPurchase.Slot.PRIMARY ? "主武器" : "手枪";
                showPickupHint(player, item, "已携带" + category + "，请先腾出该武器栏");
            }
            return;
        }
        if (handoff && player.getInventory().getFreeSlot() < 0) {
            event.setCanPickup(TriState.FALSE);
            showPickupHint(player, item, "背包已满，无法拾取枪械");
            return;
        }
        slot.mark(stack);
    }

    private static void showPickupHint(ServerPlayer player, ItemEntity item, String message) {
        // Touching an ItemEntity can trigger a pickup attempt every tick.
        if (!item.hasPickUpDelay() && player.tickCount % 20 == 0) {
            player.displayClientMessage(Component.literal(message), true);
        }
    }

    static void afterPickup(ItemEntityPickupEvent.Post event) {
        if (event.getPlayer() instanceof ServerPlayer player && isRoundParticipant(player)) {
            GunPurchase.Slot slot = GunPurchase.slotOf(event.getOriginalStack());
            if (slot != null && !findGun(carriedBy(player), slot).isEmpty()) {
                setGunScore(player, slot, true);
            }
        }
    }

    private static List<ItemStack> carriedBy(ServerPlayer player) {
        var stacks = new ArrayList<>(player.getInventory().items);
        stacks.add(player.getOffhandItem());
        stacks.add(player.containerMenu.getCarried());
        return stacks;
    }

    private static ItemStack findGun(List<ItemStack> stacks, GunPurchase.Slot slot) {
        for (ItemStack stack : stacks) {
            if (GunPurchase.slotOf(stack) == slot) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static boolean isRoundParticipant(ServerPlayer player) {
        int team = score(player, player, "cs2_team");
        return (team == 1 || team == 2) && score(player, player, "cs2_inmatch") == 1
                && score(player, player, "cs2_alive") == 1;
    }

    private static int score(ServerPlayer player, ScoreHolder holder, String name) {
        var board = player.getScoreboard();
        var objective = board.getObjective(name);
        var value = objective == null ? null : board.getPlayerScoreInfo(holder, objective);
        return value == null ? 0 : value.value();
    }

    private static void setGunScore(ServerPlayer player, GunPurchase.Slot slot, boolean present) {
        var board = player.getScoreboard();
        var objective = board.getObjective(slot == GunPurchase.Slot.PRIMARY ? "cs2_primary" : "cs2_secondary");
        if (objective != null) {
            board.getOrCreatePlayerScore(player, objective).set(present ? 1 : 0);
        }
    }
}
