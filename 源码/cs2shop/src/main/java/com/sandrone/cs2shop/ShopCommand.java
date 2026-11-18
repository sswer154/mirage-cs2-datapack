package com.sandrone.cs2shop;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;

/** /cs2shop open <菜单id> 和 /cs2shop list。 */
public final class ShopCommand {

    private ShopCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("cs2shop")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("open")
                        .then(Commands.argument("id", StringArgumentType.greedyString())
                                .executes(ctx -> {
                                    if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)) {
                                        ctx.getSource().sendFailure(Component.literal("只能由玩家执行"));
                                        return 0;
                                    }
                                    String raw = StringArgumentType.getString(ctx, "id");
                                    ResourceLocation id = raw.contains(":")
                                            ? ResourceLocation.tryParse(raw)
                                            : ResourceLocation.fromNamespaceAndPath("cs2", raw);
                                    if (id == null) {
                                        ctx.getSource().sendFailure(Component.literal("菜单 id 不合法"));
                                        return 0;
                                    }
                                    return open(player, id);
                                })))
                .then(Commands.literal("switch")
                        .then(Commands.argument("id", StringArgumentType.greedyString()).executes(ctx -> {
                            if (!(ctx.getSource().getEntity() instanceof ServerPlayer player)
                                    || !(player.containerMenu instanceof ShopMenu menu)) {
                                Cs2Shop.LOGGER.info("[cs2shop] switch 失败：没有打开的商店菜单");
                                return 0;
                            }
                            Cs2Shop.LOGGER.info("[cs2shop] switch 请求 {} -> {}", player.getScoreboardName(), StringArgumentType.getString(ctx, "id"));
                            String raw = StringArgumentType.getString(ctx, "id");
                            ResourceLocation id = raw.contains(":")
                                    ? ResourceLocation.tryParse(raw)
                                    : ResourceLocation.fromNamespaceAndPath("cs2", raw);
                            ShopDefinition def = ShopLoader.get(id);
                            if (def == null) {
                                ctx.getSource().sendFailure(Component.literal("找不到菜单: " + raw));
                                return 0;
                            }
                            return menu.switchTo(def) ? 1 : open(player, id);
                        })))
                .then(Commands.literal("close").executes(ctx -> {
                    if (ctx.getSource().getEntity() instanceof ServerPlayer player
                            && player.containerMenu instanceof ShopMenu) {
                        player.closeContainer();
                        return 1;
                    }
                    return 0;
                }))
                .then(Commands.literal("list").executes(ctx -> {
                    if (ShopLoader.all().isEmpty()) {
                        ctx.getSource().sendSuccess(() -> Component.literal("没有已加载的菜单"), false);
                        return 0;
                    }
                    StringBuilder sb = new StringBuilder("菜单: ");
                    ShopLoader.all().keySet().forEach(k -> sb.append(k).append("  "));
                    ctx.getSource().sendSuccess(() -> Component.literal(sb.toString()), false);
                    return ShopLoader.all().size();
                }))
                .then(Commands.literal("reload").executes(ctx -> {
                    ctx.getSource().getServer().reloadResources(
                            ctx.getSource().getServer().getWorldData().getDataConfiguration().dataPacks().getEnabled());
                    ctx.getSource().sendSuccess(() -> Component.literal("已重载数据包"), false);
                    return 1;
                })));
    }

    /** 给玩家打开某个菜单。 */
    public static int open(ServerPlayer player, ResourceLocation id) {
        Cs2Shop.LOGGER.info("[cs2shop] 请求打开 {} -> {}", player.getScoreboardName(), id);
        ShopDefinition def = ShopLoader.get(id);
        if (def == null) {
            player.sendSystemMessage(Component.literal("找不到菜单: " + id));
            return 0;
        }
        player.openMenu(new SimpleMenuProvider(
                (containerId, inv, p) -> new ShopMenu(containerId, inv, def, player),
                Component.literal(def.title())));
        return 1;
    }
}
