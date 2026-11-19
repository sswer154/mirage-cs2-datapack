package com.sandrone.cs2shop;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 用原版箱子界面承载商店，行数按菜单定义来（1~6 行）。
 * 点击不搬物品，而是把这一格当按钮：满足条件就执行该商品的指令，然后刷新显示。
 */
public class ShopMenu extends AbstractContainerMenu {

    private ShopDefinition def;
    private final Container container;
    private final ServerPlayer owner;
    private final int shopSlots;
    /** 实际上屏的位置 -> 条目（重排后 original slot 已经不等于显示位置）。 */
    private final Map<Integer, ShopDefinition.ShopEntry> shown = new LinkedHashMap<>();

    private static MenuType<?> typeForRows(int rows) {
        return switch (rows) {
            case 1 -> MenuType.GENERIC_9x1;
            case 2 -> MenuType.GENERIC_9x2;
            case 3 -> MenuType.GENERIC_9x3;
            case 4 -> MenuType.GENERIC_9x4;
            case 5 -> MenuType.GENERIC_9x5;
            default -> MenuType.GENERIC_9x6;
        };
    }

    public ShopMenu(int id, Inventory playerInv, ShopDefinition def, ServerPlayer owner) {
        super(typeForRows(def.rows()), id);
        this.def = def;
        this.owner = owner;
        int rows = def.rows();
        this.shopSlots = rows * 9;
        this.container = new SimpleContainer(shopSlots);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < 9; col++) {
                int index = row * 9 + col;
                this.addSlot(new Slot(container, index, 8 + col * 18, 18 + row * 18) {
                    @Override
                    public boolean mayPickup(Player player) {
                        return false;
                    }

                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return false;
                    }
                });
            }
        }
        int invY = 18 + rows * 18 + 14;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, invY + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, 8 + col * 18, invY + 58));
        }
        refresh();
    }


    /** 在同一个容器里换页：不重开界面，鼠标位置不会被打回中心。 */
    public boolean switchTo(ShopDefinition next) {
        if (next == null || next.rows() != this.def.rows()) {
            return false;
        }
        this.def = next;
        this.shown.clear();
        for (int i = 0; i < this.shopSlots; i++) {
            this.container.setItem(i, ItemStack.EMPTY);
        }
        this.refresh();
        return true;
    }

    /** 玩家的阵营分（1=T，2=CT，0=观战）。 */
    private int playerTeam() {
        var board = owner.server.getScoreboard();
        var obj = board.getObjective("cs2_team");
        if (obj == null) {
            return 0;
        }
        return board.getOrCreatePlayerScore(
                net.minecraft.world.scores.ScoreHolder.forNameOnly(owner.getScoreboardName()), obj).get();
    }

    private static final int[] INNER_COLS = {1, 2, 3, 4, 5, 6, 7};
    private static final int[] INNER_ROWS = {1, 2, 3, 4};

    private boolean hasGroups(List<ShopDefinition.ShopEntry> list) {
        for (ShopDefinition.ShopEntry e : list) {
            if (!e.group().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /** 同一 group 相邻摆放，组间空一列，排满换行。 */
    private Map<Integer, ShopDefinition.ShopEntry> reflow(List<ShopDefinition.ShopEntry> list) {
        Map<Integer, ShopDefinition.ShopEntry> out = new LinkedHashMap<>();
        // 只对带 group 的商品重排；玻璃、返回这类装饰保持原位
        List<ShopDefinition.ShopEntry> goods = new ArrayList<>();
        for (ShopDefinition.ShopEntry e : list) {
            if (!e.group().isEmpty()) {
                goods.add(e);
            }
        }
        int rowIdx = 0;
        int cursor = 0;
        int i = 0;
        while (i < goods.size() && rowIdx < INNER_ROWS.length) {
            String group = goods.get(i).group();
            List<ShopDefinition.ShopEntry> block = new ArrayList<>();
            block.add(goods.get(i));
            int j = i + 1;
            while (j < goods.size() && block.size() < 2 && goods.get(j).group().equals(group)) {
                block.add(goods.get(j));
                j++;
            }
            if (cursor > 0 && cursor + block.size() > INNER_COLS.length) {
                rowIdx++;
                cursor = 0;
                if (rowIdx >= INNER_ROWS.length) {
                    break;
                }
            }
            for (int k = 0; k < block.size(); k++) {
                int slot = INNER_ROWS[rowIdx] * 9 + INNER_COLS[cursor + k];
                out.put(slot, block.get(k));
            }
            cursor += block.size() + 1;
            i = j;
        }
        // 装饰、返回按钮等没有 group 的条目保持原位
        for (ShopDefinition.ShopEntry e : list) {
            if (e.group().isEmpty() && e.slot() >= 0) {
                out.putIfAbsent(e.slot(), e);
            }
        }
        return out;
    }

    private boolean allowedForTeam(ShopDefinition.ShopEntry e, int team) {
        return e.requiresTeam() == 0 || e.requiresTeam() == team;
    }

    /** 余额分（cs2_money）。 */
    private int playerMoney() {
        var board = owner.server.getScoreboard();
        var obj = board.getObjective("cs2_money");
        if (obj == null) {
            return 0;
        }
        return board.getOrCreatePlayerScore(
                net.minecraft.world.scores.ScoreHolder.forNameOnly(owner.getScoreboardName()), obj).get();
    }

    /** 右下角显示余额的格子。 */
    private ItemStack balanceIcon() {
        ItemStack stack = new ItemStack(net.minecraft.world.item.Items.GOLD_INGOT);
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,
                Component.literal("余额：$" + playerMoney())
                        .withStyle(s -> s.withColor(net.minecraft.ChatFormatting.GOLD)));
        stack.set(net.minecraft.core.component.DataComponents.LORE,
                new net.minecraft.world.item.component.ItemLore(java.util.List.of(
                        Component.literal("本回合可用资金").withStyle(net.minecraft.ChatFormatting.GRAY))));
        return stack;
    }

    /** 按定义把格子填成商品图标；阵营不符的直接不显示。 */
    public final void refresh() {
        for (int i = 0; i < shopSlots; i++) {
            container.setItem(i, ItemStack.EMPTY);
        }
        int team = playerTeam();
        // 过滤掉本队买不了的；剩下的按「枪族成对、族间空一列」重新排，避免出现空洞
        List<ShopDefinition.ShopEntry> visible = new ArrayList<>();
        for (ShopDefinition.ShopEntry e : def.entries()) {
            if (allowedForTeam(e, team)) {
                visible.add(e);
            }
        }
        shown.clear();
        if (shopSlots >= 54 && hasGroups(visible)) {
            for (Map.Entry<Integer, ShopDefinition.ShopEntry> en : reflow(visible).entrySet()) {
                if (en.getKey() >= 0 && en.getKey() < shopSlots) {
                    container.setItem(en.getKey(), en.getValue().display().copy());
                    shown.put(en.getKey(), en.getValue());
                }
            }
        } else {
            for (ShopDefinition.ShopEntry e : visible) {
                if (e.slot() >= 0 && e.slot() < shopSlots) {
                    container.setItem(e.slot(), e.display().copy());
                    shown.put(e.slot(), e);
                }
            }
        }
        if (def.balanceSlot() >= 0 && def.balanceSlot() < shopSlots) {
            container.setItem(def.balanceSlot(), balanceIcon());
        }
        this.broadcastChanges();
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        Cs2Shop.debug("点击槽位 {} 按钮 {}", slotId, button);
        if (clickType != ClickType.PICKUP) {
            return;
        }
        if (slotId >= 0 && slotId < shopSlots) {
            ShopDefinition.ShopEntry entry = shown.get(slotId);
            if (entry != null && !allowedForTeam(entry, playerTeam())) {
                if (player instanceof ServerPlayer sp2) {
                    sp2.displayClientMessage(Component.literal("这件装备本队买不了"), true);
                }
                entry = null;
            }
            if (entry != null && player instanceof ServerPlayer sp) {
                try {
                    for (String cond : entry.conditions()) {
                        if (!ShopActions.testCondition(sp.server, sp, cond)) {
                            sp.displayClientMessage(Component.literal("条件不满足，买不了"), true);
                            return;
                        }
                    }
                    ShopActions.runCommands(sp, entry.commands());
                } catch (Exception ex) {
                    Cs2Shop.LOGGER.warn("执行商品指令失败: {}", ex.toString());
                    sp.displayClientMessage(Component.literal("购买失败，请联系管理员查看日志"), true);
                }
            }
            // 只刷新余额格，避免整页重建导致光标跳动
            if (def.balanceSlot() >= 0 && def.balanceSlot() < shopSlots) {
                container.setItem(def.balanceSlot(), balanceIcon());
                this.broadcastChanges();
            }
            return;
        }
        // 背包区不做任何事，避免把展示物品拖出来
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
