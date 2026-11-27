package com.sandrone.cs2grenade;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.ScoreHolder;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
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
    public static final ModConfigSpec.DoubleValue PLANT_SECONDS;
    private final WaterFreeze waterFreeze = new WaterFreeze();
    private final C4Sfx c4Sfx = new C4Sfx();
    private final C4Objective c4Objective = new C4Objective();
    private boolean roundWasLive;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        DEBUG = builder
                .comment("输出逐 tick / 逐事件的调试日志（碰撞、音效、耳鸣、右键）。默认关闭。")
                .define("debug", false);
        PLANT_SECONDS = builder
                .comment("C4 安放读条时长（秒）。默认 3.2。")
                .defineInRange("plant_seconds", 3.2, 0.6, 10.0);
        SPEC = builder.build();
    }

    public Cs2Grenade(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, SPEC);
        MatchInventory.register(modBus);
        MatchSpawns.register(modBus);
        MapChunkTickets.register(modBus);
        RoundCheckpoint.register(modBus);
        RoundRewards.register();
        NeoForge.EVENT_BUS.addListener(MatchStats::registerCommands);
        TaczHitAttribution.register(NeoForge.EVENT_BUS);
        NeoForge.EVENT_BUS.addListener(this::onRightClick);
        NeoForge.EVENT_BUS.addListener(this::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(this::onLivingTick);
        NeoForge.EVENT_BUS.addListener(this::onItemToss);
        NeoForge.EVENT_BUS.addListener(this::onEntityInteract);
        NeoForge.EVENT_BUS.addListener(this::onEntityInteractSpecific);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, KillFeedHandler::onLivingDeath);
        c4Objective.register(NeoForge.EVENT_BUS);
        NeoForge.EVENT_BUS.addListener(this::onServerTick);
        NeoForge.EVENT_BUS.addListener(this::onPlayerLoggedOut);
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
        C4Plant.clearAll();
        C4Defuse.clearAll();
        event.addListener(new GrenadeLoader());
        GrenadeManager.purgeTransientVisuals(net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer());
    }

    private void onServerStarted(net.neoforged.neoforge.event.server.ServerStartedEvent event) {
        roundWasLive = false;
        c4Sfx.stopCountdown(event.getServer());
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

    private void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            waterFreeze.remove(player);
            c4Objective.onLogout(player);
            C4Plant.cancel(player, false);
            C4Defuse.cancel(player);
            c4Sfx.playerLoggedOut(player);
            GrenadeManager.clearSmokeListener(player);
            GrenadeManager.clearMaskOnReturn(player);
        }
    }

    static boolean isMatchAudioListener(ServerPlayer player) {
        var scoreboard = player.getScoreboard();
        var objective = scoreboard.getObjective("cs2_inmatch");
        var score = objective == null ? null : scoreboard.getPlayerScoreInfo(player, objective);
        var queueObjective = scoreboard.getObjective("cs2_queued_team");
        var queuedTeam = queueObjective == null ? null : scoreboard.getPlayerScoreInfo(player, queueObjective);
        return (score != null && score.value() == 1)
                || (queuedTeam != null && queuedTeam.value() >= 1 && queuedTeam.value() <= 2)
                || (player.getTeam() != null && "cs2_cast".equals(player.getTeam().getName()));
    }

    private void onServerTick(ServerTickEvent.Post event) {
        var server = event.getServer();
        var scoreboard = server.getScoreboard();
        waterFreeze.tick(server);
        var stateObjective = scoreboard.getObjective("cs2_state");
        var state = stateObjective == null ? null : scoreboard.getPlayerScoreInfo(
                ScoreHolder.forNameOnly("#state"), stateObjective);
        boolean roundIsLive = state != null && (state.value() == 3 || state.value() == 4);
        c4Objective.tick(server, state != null && state.value() == 3);
        if (roundWasLive && !roundIsLive) {
            GrenadeManager.clearRoundGrenades(server);
        }
        roundWasLive = roundIsLive;
        var counts = scoreboard.getObjective("cs2_counts");
        var pause = counts == null ? null : scoreboard.getPlayerScoreInfo(ScoreHolder.forNameOnly("#paused"), counts);
        boolean paused = pause != null && pause.value() == 1;
        GrenadeManager.tick(paused);
        if (event.getServer() != null) {
            c4Sfx.tick(server, state != null && state.value() == 4, paused);
            int now = event.getServer().getTickCount();
            for (net.minecraft.server.level.ServerPlayer p : event.getServer().getPlayerList().getPlayers()) {
                GrenadeManager.playerTick(p, now);
                C4Plant.tick(p);
                C4Defuse.tick(p);
                // 脚步声改由客户端模组 cs2footsteps 播放，服务端不再发包
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

    private void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (onEntityInteract0(event.getEntity(), event.getTarget())) {
            event.setCanceled(true);
        }
    }

    private void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (onEntityInteract0(event.getEntity(), event.getTarget())) {
            event.setCanceled(true);
        }
    }

    /** 返回 true 表示这次右键被 C4 拆除接管。 */
    private boolean onEntityInteract0(net.minecraft.world.entity.Entity e,
                                      net.minecraft.world.entity.Entity target) {
        if (!(e instanceof net.minecraft.server.level.ServerPlayer player)) {
            return false;
        }
        Cs2Grenade.debug("右键实体 {} tags={}", target.getType(), target.getTags());
        if (!target.getTags().contains("cs2_c4_hit")) {
            return false;
        }
        Cs2Grenade.debug("命中 C4 点击实体，sneak={}", player.isShiftKeyDown());
        if (!player.isShiftKeyDown()) {
            return false;
        }
        C4Defuse.toggle(player);
        return true;
    }

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
        if (data != null && data.contains("cs2_bomb")) {
            // C4：点一次开始/取消读条，不走食物消耗
            C4Plant.toggle(player, PLANT_SECONDS.get());
            event.setCanceled(true);
            return;
        }
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
