package com.sandrone.cs2shop;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** 一份菜单定义：标题、行数、每格的商品。 */
public record ShopDefinition(ResourceLocation id, String title, int rows, int balanceSlot, List<ShopEntry> entries) {

    /** 一格商品：格子序号、展示物品、点击后执行的指令、可选的前置条件。 */
    public record ShopEntry(
            int slot,
            ItemStack display,
            List<String> commands,
            List<String> conditions,
            int requiresTeam,
            String group
    ) {
        public static ShopEntry fromJson(JsonObject o) {
            int slot = o.get("slot").getAsInt();
            ItemStack stack = ItemStack.CODEC
                    .parse(com.mojang.serialization.JsonOps.INSTANCE,
                            o.has("display") ? o.get("display") : o)
                    .resultOrPartial(msg -> Cs2Shop.LOGGER.warn("商品物品解析失败: {}", msg))
                    .orElse(ItemStack.EMPTY);
            if (o.has("name")) {
                stack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,
                        Component.literal(o.get("name").getAsString()));
            }
            if (o.has("lore")) {
                List<Component> lore = new ArrayList<>();
                JsonArray arr = o.getAsJsonArray("lore");
                for (JsonElement e : arr) {
                    lore.add(Component.literal(e.getAsString()));
                }
                stack.set(net.minecraft.core.component.DataComponents.LORE,
                        new net.minecraft.world.item.component.ItemLore(lore));
            }
            List<String> cmds = new ArrayList<>();
            if (o.has("commands")) {
                for (JsonElement e : o.getAsJsonArray("commands")) {
                    cmds.add(e.getAsString());
                }
            }
            List<String> conds = new ArrayList<>();
            if (o.has("conditions")) {
                for (JsonElement e : o.getAsJsonArray("conditions")) {
                    conds.add(e.getAsString());
                }
            }
            int requiresTeam = o.has("requires_team") ? o.get("requires_team").getAsInt() : 0;
            String group = o.has("group") ? o.get("group").getAsString() : "";
            return new ShopEntry(slot, stack, cmds, conds, requiresTeam, group);
        }
    }

    public static ShopDefinition fromJson(ResourceLocation id, JsonObject o) {
        String title = o.has("title") ? o.get("title").getAsString() : id.toString();
        int rows = Math.max(1, Math.min(6, o.has("rows") ? o.get("rows").getAsInt() : 6));
        int balanceSlot = o.has("balance_slot") ? o.get("balance_slot").getAsInt() : -1;
        List<ShopEntry> entries = new ArrayList<>();
        if (o.has("items")) {
            for (JsonElement e : o.getAsJsonArray("items")) {
                try {
                    entries.add(ShopEntry.fromJson(e.getAsJsonObject()));
                } catch (Exception ex) {
                    Cs2Shop.LOGGER.warn("{} 里有一条商品解析失败: {}", id, ex.toString());
                }
            }
        }
        return new ShopDefinition(id, title, rows, balanceSlot, entries);
    }

    public int size() {
        return rows * 9;
    }

    /** 按 slot 找商品。 */
    public ShopEntry entryAt(int slot) {
        for (ShopEntry e : entries) {
            if (e.slot() == slot) {
                return e;
            }
        }
        return null;
    }

    public static ResourceLocation rl(String s) {
        return ResourceLocation.tryParse(s);
    }

    public static ItemStack defaultIcon() {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("minecraft:barrier")));
    }
}
