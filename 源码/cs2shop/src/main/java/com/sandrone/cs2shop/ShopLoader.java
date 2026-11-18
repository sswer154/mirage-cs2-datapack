package com.sandrone.cs2shop;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.server.MinecraftServer;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/** 从数据包 data/<命名空间>/shop/<id>.json 读取所有菜单。 */
public class ShopLoader implements PreparableReloadListener {

    public static final String DIRECTORY = "shop";
    private static final Map<ResourceLocation, ShopDefinition> MENUS = new HashMap<>();

    public static ShopDefinition get(ResourceLocation id) {
        return MENUS.get(id);
    }

    public static Map<ResourceLocation, ShopDefinition> all() {
        return MENUS;
    }

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager manager,
                                          ProfilerFiller prepareProfiler, ProfilerFiller applyProfiler,
                                          Executor backgroundExecutor, Executor gameExecutor) {
        return CompletableFuture.supplyAsync(() -> loadAll(manager), backgroundExecutor)
                .thenCompose(barrier::wait)
                .thenAcceptAsync(loaded -> {
                    MENUS.clear();
                    MENUS.putAll(loaded);
                    Cs2Shop.LOGGER.info("[cs2shop] 已读取 {} 个商店菜单", MENUS.size());
                }, gameExecutor);
    }

    private static Map<ResourceLocation, ShopDefinition> loadAll(ResourceManager manager) {
        Map<ResourceLocation, ShopDefinition> out = new HashMap<>();
        try {
            Map<ResourceLocation, Resource> found = manager.listResources(DIRECTORY,
                    loc -> loc.getPath().endsWith(".json"));
            for (Map.Entry<ResourceLocation, Resource> e : found.entrySet()) {
                ResourceLocation fileId = e.getKey();
                String path = fileId.getPath();
                String name = path.substring(DIRECTORY.length() + 1, path.length() - ".json".length());
                ResourceLocation menuId = ResourceLocation.fromNamespaceAndPath(fileId.getNamespace(), name);
                try (Reader reader = e.getValue().openAsReader()) {
                    JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
                    out.put(menuId, ShopDefinition.fromJson(menuId, obj));
                } catch (Exception ex) {
                    Cs2Shop.LOGGER.warn("菜单 {} 读取失败: {}", fileId, ex.toString());
                }
            }
        } catch (Exception ex) {
            Cs2Shop.LOGGER.warn("扫描商店菜单失败: {}", ex.toString());
        }
        return out;
    }

    public static void ensureLoaded(MinecraftServer server) {
        if (MENUS.isEmpty()) {
            MENUS.putAll(loadAll(server.getResourceManager()));
            Cs2Shop.LOGGER.info("[cs2shop] 首次按需读取了 {} 个商店菜单", MENUS.size());
        }
    }
}
