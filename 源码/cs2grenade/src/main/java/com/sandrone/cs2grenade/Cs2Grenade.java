package com.sandrone.cs2grenade;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;

/**
 * CS2 投掷物：服务端专用。
 * 物理全部在服务端算，客户端只靠资源包看模型，不装模组。
 */
@Mod(Cs2Grenade.MODID)
public class Cs2Grenade {
    public static final String MODID = "cs2grenade";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue DEBUG;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        DEBUG = builder
                .comment("输出逐 tick / 逐事件的调试日志（碰撞、音效、耳鸣、右键）。默认关闭。")
                .define("debug", false);
        SPEC = builder.build();
    }

    public Cs2Grenade(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, SPEC);
        NeoForge.EVENT_BUS.addListener(this::onRightClick);
        NeoForge.EVENT_BUS.addListener(this::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(this::onLivingTick);
        NeoForge.EVENT_BUS.addListener(this::onItemToss);
        NeoForge.EVENT_BUS.addListener(KillFeedHandler::onLivingDeath);
        NeoForge.EVENT_BUS.addListener(this::onServerTick);
        NeoForge.EVENT_BUS.addListener(this::onReload);
        NeoForge.EVENT_BUS.addListener(this::onChunkLoad);
        NeoForge.EVENT_BUS.addListener(this::onServerStarted);
        LOGGER.info("[cs2] CS2 投掷物已加载（服务端专用）");
    }

    /** 只在 config/cs2grenade-common.toml 的 debug=true 时输出，避免刷屏。 */
    public static void debug(String format, Object... args) {
        if (DEBUG.get()) {
            LOGGER.info("[cs2] " + format, args);
        }
    }

    private void onReload(net.neoforged.neoforge.event.AddReloadListenerEvent event) {
        event.addListener(new GrenadeLoader());
        GrenadeManager.purgeTransientVisuals(net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer());
    }

    private void onServerStarted(net.neoforged.neoforge.event.server.ServerStartedEvent event) {
        GrenadeManager.cleanupOrphans(event.getServer());
        GrenadeManager.purgeTransientVisuals(net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer());
    }

    /** 重启时落点区块未必已加载，区块一加载就按标签清掉上一局残留。 */
    private void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof net.minecraft.server.level.ServerLevel level
                && event.getChunk() instanceof net.minecraft.world.level.chunk.LevelChunk chunk) {
            GrenadeManager.cleanupChunk(level, chunk);
        }
    }

    private void onServerTick(ServerTickEvent.Post event) {
        GrenadeManager.tick();
        if (event.getServer() != null) {
            int now = event.getServer().getTickCount();
            for (net.minecraft.server.level.ServerPlayer p : event.getServer().getPlayerList().getPlayers()) {
                GrenadeManager.playerTick(p, now);
            }
        }
        if (event.getServer() != null) {
            SurgeryKit.tick(event.getServer(), event.getServer().getTickCount());
        }
    }

    /** 读条时丢物品（Q）视为打断。 */
    private void onItemToss(net.neoforged.neoforge.event.entity.item.ItemTossEvent event) {
        if (event.getPlayer() instanceof net.minecraft.server.level.ServerPlayer sp) {
            SurgeryKit.requestCancel(sp, sp.getServer() == null ? 0 : sp.getServer().getTickCount());
        }
    }

    /** 读条期间每一 tick 都把疾跑状态按掉，避免客户端一直保持疾跑。 */
    private void onLivingTick(net.neoforged.neoforge.event.tick.EntityTickEvent.Post event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player
                && SurgeryKit.isChanneling(player.getUUID())) {
            player.setSprinting(false);
        }
    }

    /** 对着方块右键时走的是这个事件，医疗用品也要接管。 */
    private void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)) {
            return;
        }
        var stack = event.getItemStack();
        var data = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        if (data == null || !SurgeryKit.isMedical(stack)) {
            return;
        }
        if (SurgeryKit.tryStart(player, stack, player.getServer().getTickCount())) {
            event.setCanceled(true);
        }
    }

    private void onRightClick(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)) {
            return;
        }
        var stack = event.getItemStack();
        var data = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        if (data == null || !data.contains("cs2_util")) {
            return;
        }
        Cs2Grenade.debug("右键物品 util={}", data.copyTag().getString("cs2_util"));
        if (SurgeryKit.isMedical(stack)) {
            if (SurgeryKit.tryStart(player, stack, player.getServer().getTickCount())) {
                event.setCanceled(true);
            }
            return;
        }
        if (GrenadeManager.requestThrow(player)) {
            event.setCanceled(true);
        }
    }
}
