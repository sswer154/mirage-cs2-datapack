package com.sandrone.cs2fairplay;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import org.joml.Matrix4f;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

final class SpectatorGunRenderer {
    private static final String TACZ = "com.tacz.guns.";
    private static Adapter adapter;
    private static boolean adapterLoaded;
    private static Object posedDisplay;
    private static final Map<String, Clip> clips = new HashMap<>();
    private static AbstractClientPlayer actor;
    private static Shot shot;
    private static float previousAim;
    private static float currentAim;
    private static int aimTick;

    private SpectatorGunRenderer() {}

    static void register() {
        if (!ModList.get().isLoaded("tacz")) return;
        try {
            var event = Class.forName(TACZ + "api.event.common.GunShootEvent").asSubclass(Event.class);
            Method shooter = event.getMethod("getShooter");
            Method side = event.getMethod("getLogicalSide");
            Method gun = event.getMethod("getGunItemStack");
            NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, event, notice -> {
                try {
                    if (side.invoke(notice) != LogicalSide.CLIENT) return;
                    var target = SpectatorHands.target(Minecraft.getInstance());
                    if (target != null && shooter.invoke(notice) == target && loadAdapter()) {
                        var stack = (ItemStack) gun.invoke(notice);
                        shot = new Shot(target, adapter.gunId.invoke(stack.getItem(), stack), System.nanoTime());
                    }
                } catch (ReflectiveOperationException exception) {
                    com.mojang.logging.LogUtils.getLogger().warn("Could not read observed TaCZ shot", exception);
                }
            });
        } catch (ReflectiveOperationException | LinkageError exception) {
            com.mojang.logging.LogUtils.getLogger().warn("TaCZ spectator shooting event is unavailable", exception);
        }
    }

    static boolean render(AbstractClientPlayer target, ItemStack stack, PoseStack pose, int light, float partialTick) {
        if (!loadAdapter()) return false;
        if (stack.isEmpty()) {
            reset();
            return false;
        }
        try {
            return adapter.render(target, stack, pose, light, partialTick);
        } catch (ReflectiveOperationException | LinkageError exception) {
            com.mojang.logging.LogUtils.getLogger().warn("Could not render the observed TaCZ gun", exception);
            adapter = null;
            reset();
            return false;
        }
    }

    private static boolean loadAdapter() {
        if (!adapterLoaded) {
            adapter = Adapter.load();
            adapterLoaded = true;
        }
        return adapter != null;
    }

    static void reset() {
        posedDisplay = null;
        clips.clear();
        actor = null;
        shot = null;
    }

    private record Shot(AbstractClientPlayer shooter, Object gunId, long start) {}

    private record Clip(Object pose, float duration) {}

    private record Adapter(Class<?> gun, Method gunId, Method display, Method model, Method texture,
                           Method transparent, Method idlePath, Method ironPath, Method inverse,
                           Method matrixLerp, Method getHands, Method setHands,
                           Method draw, Method restorePose, Animation animation, GunState state) {
        static Adapter load() {
            if (!ModList.get().isLoaded("tacz")) return null;
            try {
                Class<?> data = Class.forName(TACZ + "client.resource.GunDisplayInstance");
                Class<?> gunModel = Class.forName(TACZ + "client.model.BedrockGunModel");
                Class<?> gunItem = Class.forName(TACZ + "api.item.IGun");
                Class<?> renderer = Class.forName(TACZ + "client.renderer.item.AnimateGeoItemRenderer");
                return new Adapter(
                        gunItem, gunItem.getMethod("getGunId", ItemStack.class),
                        Class.forName(TACZ + "api.TimelessAPI").getMethod("getGunDisplay", ItemStack.class),
                        data.getMethod("getGunModel"), data.getMethod("getModelTexture"),
                        data.getMethod("enablesTransparency"),
                        gunModel.getMethod("getIdleSightPath"), gunModel.getMethod("getIronSightPath"),
                        renderer.getMethod("getPositioningNodeInverse", List.class),
                        Class.forName(TACZ + "util.math.MathUtil").getMethod("applyMatrixLerp",
                                Matrix4f.class, Matrix4f.class, Matrix4f.class, float.class),
                        gunModel.getMethod("getRenderHand"), gunModel.getMethod("setRenderHand", boolean.class),
                        gunModel.getMethod("render", PoseStack.class, ItemStack.class, ItemDisplayContext.class,
                                RenderType.class, int.class, int.class),
                        gunModel.getMethod("cleanAnimationTransform"), Animation.load(data), GunState.load());
            } catch (ReflectiveOperationException | LinkageError exception) {
                com.mojang.logging.LogUtils.getLogger().warn("TaCZ spectator view model is unavailable", exception);
                return null;
            }
        }

        boolean render(AbstractClientPlayer target, ItemStack stack, PoseStack pose, int light, float partialTick)
                throws ReflectiveOperationException {
            if (!gun.isInstance(stack.getItem())) {
                reset();
                return false;
            }
            Object data = ((Optional<?>) display.invoke(null, stack)).orElse(null);
            if (data == null) return false;
            Object viewModel = model.invoke(data);
            if (viewModel == null) return false;
            if (shot != null && (shot.shooter != target || !Objects.equals(shot.gunId, gunId.invoke(stack.getItem(), stack)))) {
                shot = null;
            }
            float aim = state.aim(target);
            boolean switchedGun = posedDisplay != data;
            if (switchedGun) {
                clips.clear();
                posedDisplay = data;
            }
            if (actor != target || switchedGun || target.tickCount > aimTick + 1) {
                previousAim = aim;
            } else if (target.tickCount != aimTick) {
                previousAim = currentAim;
            }
            aimTick = target.tickCount;
            currentAim = aim;
            actor = target;
            Clip idle = clip(data, "static_idle");
            if (idle == null) idle = clip(data, "idle");
            var skin = (ResourceLocation) texture.invoke(data);
            var type = (boolean) transparent.invoke(data) ? RenderType.entityTranslucent(skin) : RenderType.entityCutout(skin);
            boolean handsVisible = (boolean) getHands.invoke(viewModel);
            pose.pushPose();
            try {
                if (idle != null) {
                    animation.sample(idle, 0);
                    animate(target, data, partialTick);
                }
                pose.translate(0, 1.5, 0);
                pose.mulPose(Axis.ZP.rotationDegrees(180));
                position(pose, viewModel, Mth.lerp(partialTick, previousAim, currentAim));
                setHands.invoke(viewModel, idle != null);
                draw.invoke(viewModel, pose, stack, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,
                        type, light, OverlayTexture.NO_OVERLAY);
            } finally {
                pose.popPose();
                setHands.invoke(viewModel, handsVisible);
                if (idle != null) restorePose.invoke(viewModel);
            }
            return true;
        }

        private void position(PoseStack pose, Object viewModel, float aim) throws ReflectiveOperationException {
            var hip = (Matrix4f) inverse.invoke(null, idlePath.invoke(viewModel));
            List<?> sights = (List<?>) ironPath.invoke(viewModel);
            if (aim > 0 && sights != null && !sights.isEmpty()) {
                var sight = (Matrix4f) inverse.invoke(null, sights);
                var blended = new Matrix4f(hip);
                matrixLerp.invoke(null, hip, sight, blended, aim);
                hip = blended;
            }
            pose.translate(0, 1.5, 0);
            pose.mulPose(hip);
            pose.translate(0, -1.5, 0);
        }

        private Clip clip(Object data, String name) throws ReflectiveOperationException {
            if (!clips.containsKey(name)) clips.put(name, animation == null ? null : animation.load(data, name));
            return clips.get(name);
        }

        private void animate(AbstractClientPlayer target, Object data, float partialTick)
                throws ReflectiveOperationException {
            Object reload = state.reload.invoke(target);
            Enum<?> mode = (Enum<?>) state.type.invoke(reload);
            if (!mode.name().equals("NOT_RELOADING")) {
                String name = mode.name().startsWith("EMPTY_") ? "reload_empty" : "reload_tactical";
                sampleCountdown(clip(data, name), (long) state.remaining.invoke(reload), partialTick);
                return;
            }
            long drawing = (long) state.draw.invoke(target);
            if (drawing > 0) {
                sampleCountdown(clip(data, "draw"), drawing, partialTick);
                return;
            }
            if (shot != null) {
                Clip shooting = clip(data, "shoot");
                float elapsed = (System.nanoTime() - shot.start) / 1_000_000_000.0f;
                if (shooting != null && elapsed <= shooting.duration) animation.sample(shooting, elapsed);
                else shot = null;
            }
        }

        private void sampleCountdown(Clip clip, long milliseconds, float partialTick) throws ReflectiveOperationException {
            if (clip != null) {
                float elapsed = clip.duration - milliseconds / 1000.0f + partialTick / 20.0f;
                animation.sample(clip, Mth.clamp(elapsed, 0, clip.duration));
            }
        }
    }

    private record GunState(Method aiming, Method reload, Method type, Method remaining, Method draw) {
        static GunState load() throws ReflectiveOperationException {
            Class<?> operator = Class.forName(TACZ + "api.entity.IGunOperator");
            Class<?> reload = Class.forName(TACZ + "api.entity.ReloadState");
            return new GunState(operator.getMethod("getSynAimingProgress"), operator.getMethod("getSynReloadState"),
                    reload.getMethod("getStateType"), reload.getMethod("getCountDown"),
                    operator.getMethod("getSynDrawCoolDown"));
        }

        float aim(LivingEntity entity) throws ReflectiveOperationException {
            return Mth.clamp((float) aiming.invoke(entity), 0, 1);
        }
    }

    // Copy clips before binding model transforms. Sampling updates bones without playing TaCZ's sounds.
    private record Animation(Method stateMachine, Method controller, Field prototypes, Field listeners,
                             Constructor<?> copy, Method bind, Method update, Method length) {
        static Animation load(Class<?> data) throws ReflectiveOperationException {
            String prefix = TACZ + "api.client.animation.";
            Class<?> type = Class.forName(prefix + "ObjectAnimation");
            Class<?> animationController = Class.forName(prefix + "AnimationController");
            Field poses = animationController.getDeclaredField("prototypes");
            Field listenerSupplier = animationController.getDeclaredField("listenerSupplier");
            if (!poses.trySetAccessible() || !listenerSupplier.trySetAccessible()) return null;
            return new Animation(data.getMethod("getAnimationStateMachine"),
                    Class.forName(prefix + "statemachine.AnimationStateMachine").getMethod("getAnimationController"),
                    poses, listenerSupplier, type.getConstructor(type),
                    type.getMethod("applyAnimationListeners", Class.forName(prefix + "AnimationListenerSupplier")),
                    type.getMethod("update", boolean.class, float.class), type.getMethod("getMaxEndTimeS"));
        }

        Clip load(Object data, String name) throws ReflectiveOperationException {
            Object state = stateMachine.invoke(data);
            if (state == null) return null;
            Object animationController = controller.invoke(state);
            var animations = (Map<?, ?>) prototypes.get(animationController);
            Object prototype = animations.get(name);
            if (prototype == null) return null;
            Object pose = copy.newInstance(prototype);
            bind.invoke(pose, listeners.get(animationController));
            return new Clip(pose, (float) length.invoke(pose));
        }

        void sample(Clip clip, float seconds) throws ReflectiveOperationException {
            update.invoke(clip.pose, false, seconds * 1_000_000_000.0f);
        }
    }
}
