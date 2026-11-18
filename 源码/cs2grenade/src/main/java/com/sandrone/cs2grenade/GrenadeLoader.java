package com.sandrone.cs2grenade;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/** 从数据包 data/<命名空间>/grenade/<id>.json 读取投掷物参数。 */
public class GrenadeLoader implements PreparableReloadListener {

    public static final String DIRECTORY = "grenade";
    private static final Map<ResourceLocation, GrenadeType> TYPES = new HashMap<>();

    public static GrenadeType get(ResourceLocation id) {
        return TYPES.get(id);
    }

    public static Map<ResourceLocation, GrenadeType> all() {
        return TYPES;
    }

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager manager,
                                          ProfilerFiller prepareProfiler, ProfilerFiller applyProfiler,
                                          Executor backgroundExecutor, Executor gameExecutor) {
        return CompletableFuture.supplyAsync(() -> loadAll(manager), backgroundExecutor)
                .thenCompose(barrier::wait)
                .thenAcceptAsync(loaded -> {
                    TYPES.clear();
                    TYPES.putAll(loaded);
                    Cs2Grenade.LOGGER.info("[cs2] 已读取 {} 种投掷物配置", TYPES.size());
                }, gameExecutor);
    }

    private static Map<ResourceLocation, GrenadeType> loadAll(ResourceManager manager) {
        Map<ResourceLocation, GrenadeType> out = new HashMap<>();
        try {
            Map<ResourceLocation, Resource> found = manager.listResources(DIRECTORY,
                    loc -> loc.getPath().endsWith(".json"));
            for (Map.Entry<ResourceLocation, Resource> e : found.entrySet()) {
                ResourceLocation fileId = e.getKey();
                String path = fileId.getPath();
                String name = path.substring(DIRECTORY.length() + 1, path.length() - ".json".length());
                ResourceLocation typeId = ResourceLocation.fromNamespaceAndPath(fileId.getNamespace(), name);
                try (Reader reader = e.getValue().openAsReader()) {
                    JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
                    out.put(typeId, GrenadeType.fromJson(typeId, obj));
                } catch (Exception ex) {
                    Cs2Grenade.LOGGER.warn("投掷物配置 {} 读取失败: {}", fileId, ex.toString());
                }
            }
        } catch (Exception ex) {
            Cs2Grenade.LOGGER.warn("扫描投掷物配置失败: {}", ex.toString());
        }
        return out;
    }

    public static void ensureLoaded(MinecraftServer server) {
        if (TYPES.isEmpty()) {
            TYPES.putAll(loadAll(server.getResourceManager()));
            Cs2Grenade.LOGGER.info("[cs2] 首次按需读取了 {} 种投掷物配置", TYPES.size());
        }
    }
}
