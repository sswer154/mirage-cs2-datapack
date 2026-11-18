package com.sandrone.cs2shop;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.slf4j.Logger;

/**
 * CS2 商店界面：服务端专用。
 * 用原版箱子菜单渲染，内容来自数据包 data/&lt;命名空间&gt;/shop/*.json，点击执行指令。
 * 客户端不需要装这个模组（mods.toml 里已忽略客户端版本）。
 */
@Mod(Cs2Shop.MODID)
public class Cs2Shop {
    public static final String MODID = "cs2shop";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Cs2Shop(IEventBus modBus, ModContainer container) {
        NeoForge.EVENT_BUS.addListener(this::onReload);
        NeoForge.EVENT_BUS.addListener(this::onCommands);
        NeoForge.EVENT_BUS.addListener(this::onRightClick);
        NeoForge.EVENT_BUS.addListener(this::onContainerClose);
        LOGGER.info("CS2 Shop GUI 已加载（服务端专用）");
    }

    private void onContainerClose(net.neoforged.neoforge.event.entity.player.PlayerContainerEvent.Close event) {
        LOGGER.info("[cs2shop] 容器关闭: {} ({})", event.getContainer().getClass().getSimpleName(), event.getEntity().getScoreboardName());
    }

    private void onReload(AddReloadListenerEvent event) {
        event.addListener(new ShopLoader());
    }

    private void onCommands(RegisterCommandsEvent event) {
        ShopCommand.register(event.getDispatcher());
    }

    /** 手上拿着商店道具右键，直接打开界面。 */
    private void onRightClick(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)) {
            return;
        }
        var stack = event.getItemStack();
        var data = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        if (data == null || !data.contains("cs2_shop_item")) {
            return;
        }
        event.setCanceled(true);
        String id = data.copyTag().getString("cs2_shop_id");
        if (id.isEmpty()) {
            id = "main";
        }
        var target = id.contains(":")
                ? net.minecraft.resources.ResourceLocation.tryParse(id)
                : net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("cs2", id);
        if (target == null) {
            target = net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("cs2", "main");
        }
        ShopCommand.open(player, target);
    }
}
