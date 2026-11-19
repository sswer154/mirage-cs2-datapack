package com.sandrone.cs2grenade;

import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** Stores the original inventory in the same player data as the current inventory. */
final class MatchInventory {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Cs2Grenade.MODID);
    private static final DeferredHolder<AttachmentType<?>, AttachmentType<CompoundTag>> SAVED =
            ATTACHMENTS.register("match_inventory", () -> AttachmentType.builder(() -> new CompoundTag())
                    .serialize(CompoundTag.CODEC).copyOnDeath().build());

    private MatchInventory() {
    }

    static void register(IEventBus modBus) {
        ATTACHMENTS.register(modBus);
        NeoForge.EVENT_BUS.addListener(MatchInventory::registerCommands);
    }

    private static void registerCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("cs2inventory")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("save").executes(context ->
                        save(context.getSource().getPlayerOrException())))
                .then(Commands.literal("has_saved").executes(context ->
                        context.getSource().getPlayerOrException().hasData(SAVED) ? 1 : 0))
                .then(Commands.literal("restore").executes(context ->
                        restore(context.getSource().getPlayerOrException()))));
    }

    static int save(ServerPlayer player) {
        if (player.hasData(SAVED)) {
            return 1;
        }
        player.closeContainer();
        Inventory inventory = player.getInventory();
        CompoundTag snapshot = new CompoundTag();
        snapshot.put("Items", inventory.save(new ListTag()));
        snapshot.putInt("Selected", inventory.selected);
        player.setData(SAVED, snapshot);
        return 1;
    }

    static int restore(ServerPlayer player) {
        if (!player.hasData(SAVED)) {
            return 0;
        }
        CompoundTag snapshot = player.getData(SAVED);
        if (!(snapshot.get("Items") instanceof ListTag items)) {
            return invalidSnapshot(player);
        }
        // Decode into a temporary inventory so a missing item or corrupt slot cannot
        // replace the player's current inventory with only part of the snapshot.
        Inventory restored = new Inventory(player);
        restored.load(items);
        int restoredStacks = 0;
        for (int slot = 0; slot < restored.getContainerSize(); slot++) {
            if (!restored.getItem(slot).isEmpty()) {
                restoredStacks++;
            }
        }
        if (restoredStacks != items.size()) {
            return invalidSnapshot(player);
        }
        int selected = snapshot.getInt("Selected");
        restored.selected = selected >= 0 && selected < 9 ? selected : 0;

        player.closeContainer();
        Inventory inventory = player.getInventory();
        inventory.replaceWith(restored);
        inventory.setChanged();
        player.removeData(SAVED);

        player.inventoryMenu.broadcastFullState();
        player.connection.send(new ClientboundSetCarriedItemPacket(inventory.selected));
        player.sendSystemMessage(Component.literal("已返还入队前的背包物品。"));
        return 1;
    }

    private static int invalidSnapshot(ServerPlayer player) {
        Cs2Grenade.LOGGER.error("Cannot restore the saved CS2 inventory for {}", player.getUUID());
        player.sendSystemMessage(Component.literal("暂时无法返还背包，保存记录仍在。请联系管理员。"));
        return 0;
    }
}
