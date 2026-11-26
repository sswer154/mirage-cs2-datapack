package com.sandrone.cs2shop;

import java.util.ArrayList;
import java.util.Set;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

final class GunPurchase {
    private static final ResourceLocation GUN = ResourceLocation.fromNamespaceAndPath("tacz", "modern_kinetic_gun");
    private static final String PURCHASE_PRICE = "cs2_shop_purchase_price";
    private static final String PURCHASE_OWNER = "cs2_shop_purchase_owner";

    enum Slot {
        PRIMARY("cs2_primary_gun", Set.of(
                "bf1:lewis", "bf1:m2_2", "bf1:man_m95",
                "bf1:vp1915", "tacz:aa12", "tacz:ai_awp",
                "tacz:ak47", "tacz:aug", "tacz:db_long",
                "tacz:fn_fal", "tacz:g36k", "tacz:hk_mp5a5",
                "tacz:m1014", "tacz:m16a4", "tacz:m249",
                "tacz:m4a1", "tacz:m870", "tacz:p90",
                "tacz:qbz_191", "tacz:qbz_95", "tacz:scar_l",
                "tacz:sks_tactical", "tacz:spr15hb", "tacz:springfield1873",
                "tacz:ump45")),
        SECONDARY("cs2_secondary_gun", Set.of(
                "tacz:glock_17", "tacz:m9a4", "tacz:cz75", "tacz:m1911", "tacz:deagle",
                "bf1:handgun", "bf1:sw_model3", "bf1:obrez"));

        private final String marker;
        private final Set<String> legacyIds;

        Slot(String marker, Set<String> legacyIds) {
            this.marker = marker;
            this.legacyIds = legacyIds;
        }

        boolean matches(CompoundTag tag) {
            return tag.getBoolean(marker) || legacyIds.contains(tag.getString("GunId"));
        }

        void mark(ItemStack stack) {
            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putBoolean(marker, true));
        }
    }

    private GunPurchase() {}

    static int buy(ServerPlayer player, Slot slot, int price, ItemStack gun) {
        var newData = gun.get(DataComponents.CUSTOM_DATA);
        if (!isGun(gun) || newData == null || !newData.copyTag().getBoolean(slot.marker)) {
            return 0;
        }
        var board = player.getScoreboard();
        var money = board.getObjective("cs2_money");
        var balance = money == null ? null : board.getPlayerScoreInfo(player, money);
        if (balance == null) {
            return 0;
        }

        var inventory = player.getInventory();
        var replaced = new ArrayList<Integer>();
        int refund = 0;
        for (int index = 0; index < inventory.getContainerSize(); index++) {
            if (index >= Inventory.INVENTORY_SIZE && index != Inventory.SLOT_OFFHAND) {
                continue;
            }
            var oldGun = inventory.getItem(index);
            var data = oldGun.get(DataComponents.CUSTOM_DATA);
            if (!isGun(oldGun) || data == null) {
                continue;
            }
            var tag = data.copyTag();
            if (!slot.matches(tag)) {
                continue;
            }
            replaced.add(index);
            // The match allows one gun per slot. Legacy duplicates may be removed, but only one is refunded.
            if (tag.getBoolean(slot.marker) && tag.contains(PURCHASE_PRICE, Tag.TAG_INT)
                    && tag.hasUUID(PURCHASE_OWNER) && player.getUUID().equals(tag.getUUID(PURCHASE_OWNER))) {
                refund = Math.max(refund, tag.getInt(PURCHASE_PRICE));
            }
        }

        long remaining = (long) balance.value() + refund - price;
        if (remaining < 0) {
            player.sendSystemMessage(Component.literal("资金不足：新枪售价 $" + price + "，旧枪可退 $" + refund + "。"));
            return 0;
        }
        int target = inventory.getFreeSlot();
        // The match reserves the offhand for the ammo box.
        if (!replaced.isEmpty() && replaced.getFirst() < Inventory.INVENTORY_SIZE) {
            target = replaced.getFirst();
        }
        if (target < 0) {
            player.sendSystemMessage(Component.literal("背包已满，无法购买枪械。"));
            return 0;
        }
        int newBalance = Math.toIntExact(remaining);
        CustomData.update(DataComponents.CUSTOM_DATA, gun, tag -> {
            tag.putInt(PURCHASE_PRICE, price);
            tag.putUUID(PURCHASE_OWNER, player.getUUID());
        });
        for (int index : replaced) {
            inventory.setItem(index, ItemStack.EMPTY);
        }
        inventory.setItem(target, gun);
        inventory.setChanged();
        board.getOrCreatePlayerScore(player, money).set(newBalance);
        if (refund > 0) {
            player.sendSystemMessage(Component.literal("旧枪退款 $" + refund + "。"));
        }
        return 1;
    }

    static Slot slotOf(ItemStack stack) {
        var data = stack.get(DataComponents.CUSTOM_DATA);
        if (!isGun(stack) || data == null) {
            return null;
        }
        var tag = data.copyTag();
        if (Slot.PRIMARY.matches(tag)) {
            return Slot.PRIMARY;
        }
        return Slot.SECONDARY.matches(tag) ? Slot.SECONDARY : null;
    }

    static void clearPurchase(ItemStack stack) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.remove(PURCHASE_PRICE);
            tag.remove(PURCHASE_OWNER);
        });
    }

    private static boolean isGun(ItemStack stack) {
        return !stack.isEmpty() && GUN.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }
}
