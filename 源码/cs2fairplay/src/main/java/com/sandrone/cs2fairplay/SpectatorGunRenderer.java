package com.sandrone.cs2fairplay;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.nbt.CompoundTag;
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
import java.util.ArrayList;
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
    private static Clip action;
    private static long actionStart;

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
        action = null;
    }

    static SpectatorHands.ObservedShot shotSound(ItemStack stack, ResourceLocation played) {
        if (!loadAdapter() || !adapter.gun.isInstance(stack.getItem())) return null;
        try {
            return adapter.shotSound(stack, played);
        } catch (ReflectiveOperationException | LinkageError exception) {
            return null;
        }
    }

    private record Shot(AbstractClientPlayer shooter, Object gunId, long start) {}

    private record Clip(Object pose, float duration) {}

    private record Adapter(Class<?> gun, Method gunId, Method display, Method model, Method texture,
                           Method transparent, Method idlePath, Method ironPath, Method inverse,
                           Method matrixLerp, Method getHands, Method setHands,
                           Method draw, Method restorePose, Animation animation, GunState state, Optics optics,
                           Method sound, String fire3p, String fire1p, String silence3p, String silence1p) {
        static Adapter load() {
            if (!ModList.get().isLoaded("tacz")) return null;
            try {
                Class<?> data = Class.forName(TACZ + "client.resource.GunDisplayInstance");
                Class<?> gunModel = Class.forName(TACZ + "client.model.BedrockGunModel");
                Class<?> gunItem = Class.forName(TACZ + "api.item.IGun");
                Class<?> renderer = Class.forName(TACZ + "client.event.FirstPersonRenderGunEvent");
                Method inverse = renderer.getDeclaredMethod("getPositioningNodeInverse", List.class);
                if (!inverse.trySetAccessible()) return null;
                Class<?> sounds = Class.forName(TACZ + "sound.SoundManager");
                return new Adapter(
                        gunItem, gunItem.getMethod("getGunId", ItemStack.class),
                        Class.forName(TACZ + "api.TimelessAPI").getMethod("getGunDisplay", ItemStack.class),
                        data.getMethod("getGunModel"), data.getMethod("getModelTexture"),
                        data.getMethod("enablesTransparency"),
                        gunModel.getMethod("getIdleSightPath"), gunModel.getMethod("getIronSightPath"),
                        inverse,
                        Class.forName(TACZ + "util.math.MathUtil").getMethod("applyMatrixLerp",
                                Matrix4f.class, Matrix4f.class, Matrix4f.class, float.class),
                        gunModel.getMethod("getRenderHand"), gunModel.getMethod("setRenderHand", boolean.class),
                        gunModel.getMethod("render", PoseStack.class, ItemStack.class, ItemDisplayContext.class,
                                RenderType.class, int.class, int.class),
                        gunModel.getMethod("cleanAnimationTransform"), Animation.load(data), GunState.load(),
                        Optics.load(gunItem, gunModel), data.getMethod("getSounds", String.class),
                        (String) sounds.getField("SHOOT_3P_SOUND").get(null), (String) sounds.getField("SHOOT_SOUND").get(null),
                        (String) sounds.getField("SILENCE_3P_SOUND").get(null), (String) sounds.getField("SILENCE_SOUND").get(null));
            } catch (ReflectiveOperationException | LinkageError exception) {
                com.mojang.logging.LogUtils.getLogger().warn("TaCZ spectator view model is unavailable", exception);
                return null;
            }
        }

        SpectatorHands.ObservedShot shotSound(ItemStack stack, ResourceLocation played) throws ReflectiveOperationException {
            Object displayData = ((Optional<?>) display.invoke(null, stack)).orElse(null);
            if (displayData == null) return null;
            boolean suppressed = played.equals(sound.invoke(displayData, silence3p));
            if (!suppressed && !played.equals(sound.invoke(displayData, fire3p))) return null;
            var resource = (ResourceLocation) sound.invoke(displayData, suppressed ? silence1p : fire1p);
            if (resource == null) return null;
            var path = resource.withPath(name -> "tacz_sounds/" + name + ".ogg");
            return Minecraft.getInstance().getResourceManager().getResource(path).isPresent()
                    ? new SpectatorHands.ObservedShot(resource, suppressed ? 0.6f : 0.8f) : null;
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
                if (actor != target || switchedGun) action = null;
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
                    animate(target, stack, data);
                }
                pose.translate(0, 1.5, 0);
                pose.mulPose(Axis.ZP.rotationDegrees(180));
                position(pose, viewModel, stack, (float) Mth.smoothstep(Mth.lerp(partialTick, previousAim, currentAim)));
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

        private void position(PoseStack pose, Object viewModel, ItemStack stack, float aim) throws ReflectiveOperationException {
            var hip = (Matrix4f) inverse.invoke(null, idlePath.invoke(viewModel));
            if (aim > 0) {
                List<?> sights = optics.sightPath(viewModel, stack);
                if (sights == null || sights.isEmpty()) sights = (List<?>) ironPath.invoke(viewModel);
                if (sights != null && !sights.isEmpty()) {
                    var sight = (Matrix4f) inverse.invoke(null, sights);
                    var blended = new Matrix4f(hip);
                    matrixLerp.invoke(null, hip, sight, blended, aim);
                    hip = blended;
                }
            }
            pose.translate(0, 1.5, 0);
            pose.mulPose(hip);
            pose.translate(0, -1.5, 0);
        }

        private Clip clip(Object data, String name) throws ReflectiveOperationException {
            if (!clips.containsKey(name)) clips.put(name, animation == null ? null : animation.load(data, name));
            return clips.get(name);
        }

        private void animate(AbstractClientPlayer target, ItemStack stack, Object data)
                throws ReflectiveOperationException {
            Object reload = state.reload.invoke(target);
            Enum<?> mode = (Enum<?>) state.type.invoke(reload);
            if (!mode.name().equals("NOT_RELOADING")) {
                String name = mode.name().startsWith("EMPTY_") ? "reload_empty" : "reload_tactical";
                Clip reloadClip = optics.extendedMagazine(stack) ? clip(data, name + "_xmag") : null;
                sampleAction(reloadClip != null ? reloadClip : clip(data, name), (long) state.remaining.invoke(reload));
                return;
            }
            long drawing = (long) state.draw.invoke(target);
            if (drawing > 0) {
                sampleAction(clip(data, "draw"), drawing);
                return;
            }
            if ((boolean) state.bolt.invoke(target)) {
                sampleAction(clip(data, "bolt"), -1);
                return;
            }
            action = null;
            if (shot != null) {
                Clip shooting = clip(data, "shoot");
                float elapsed = (System.nanoTime() - shot.start) / 1_000_000_000.0f;
                if (shooting != null && elapsed <= shooting.duration) animation.sample(shooting, elapsed);
                else shot = null;
            }
        }

        private void sampleAction(Clip clip, long remaining) throws ReflectiveOperationException {
            if (clip == null) {
                action = null;
                return;
            }
            long now = System.nanoTime();
            if (action != clip) {
                action = clip;
                float initial = remaining < 0 ? 0 : Math.max(0, clip.duration - remaining / 1000f);
                actionStart = now - (long) (initial * 1_000_000_000L);
            }
            animation.sample(clip, Math.min(clip.duration, (now - actionStart) / 1_000_000_000f));
        }
    }

    private record Optics(Object scope, Object magazine, Method attachment, Method builtIn, Method empty,
                          Method tag, Method zoom, Method mount, Method index, Method views, Method scopeModel,
                          Method scopePath) {
        static Optics load(Class<?> gun, Class<?> gunModel) throws ReflectiveOperationException {
            var attachment = Class.forName(TACZ + "api.item.attachment.AttachmentType");
            var index = Class.forName(TACZ + "client.resource.index.ClientAttachmentIndex");
            return new Optics(attachment.getField("SCOPE").get(null), attachment.getField("EXTENDED_MAG").get(null),
                    gun.getMethod("getAttachmentId", ItemStack.class, attachment),
                    gun.getMethod("getBuiltInAttachmentId", ItemStack.class, attachment),
                    Class.forName(TACZ + "api.DefaultAssets").getMethod("isEmptyAttachmentId", ResourceLocation.class),
                    gun.getMethod("getAttachmentTag", ItemStack.class, attachment),
                    Class.forName(TACZ + "api.item.nbt.AttachmentItemDataAccessor")
                            .getMethod("getZoomNumberFromTag", CompoundTag.class),
                    gunModel.getMethod("getScopePosPath"), Class.forName(TACZ + "api.TimelessAPI")
                            .getMethod("getClientAttachmentIndex", ResourceLocation.class),
                    index.getMethod("getViews"), index.getMethod("getAttachmentModel"),
                    Class.forName(TACZ + "client.model.BedrockAttachmentModel").getMethod("getScopeViewPath", int.class));
        }

        private Object attachmentId(ItemStack stack, Object type) throws ReflectiveOperationException {
            Object id = attachment.invoke(stack.getItem(), stack, type);
            return (boolean) empty.invoke(null, id) ? builtIn.invoke(stack.getItem(), stack, type) : id;
        }

        boolean extendedMagazine(ItemStack stack) throws ReflectiveOperationException {
            return !(boolean) empty.invoke(null, attachmentId(stack, magazine));
        }

        List<?> sightPath(Object model, ItemStack stack) throws ReflectiveOperationException {
            Object id = attachmentId(stack, scope);
            if ((boolean) empty.invoke(null, id)) return null;
            List<?> mountPath = (List<?>) mount.invoke(model);
            if (mountPath == null || mountPath.isEmpty()) return null;
            Object attachmentIndex = ((Optional<?>) index.invoke(null, id)).orElse(null);
            if (attachmentIndex == null) return mountPath;
            int[] viewNumbers = (int[]) views.invoke(attachmentIndex);
            Object scopeMesh = scopeModel.invoke(attachmentIndex);
            if (scopeMesh == null || viewNumbers == null || viewNumbers.length == 0) return mountPath;
            int zoomIndex = (int) zoom.invoke(null, tag.invoke(stack.getItem(), stack, scope));
            List<?> viewPath = (List<?>) scopePath.invoke(scopeMesh, viewNumbers[Math.floorMod(zoomIndex, viewNumbers.length)] - 1);
            if (viewPath == null || viewPath.isEmpty()) return mountPath;
            var path = new ArrayList<Object>(mountPath);
            path.addAll(viewPath);
            return path;
        }
    }

    private record GunState(Method aiming, Method reload, Method type, Method remaining, Method draw, Method bolt) {
        static GunState load() throws ReflectiveOperationException {
            Class<?> operator = Class.forName(TACZ + "api.entity.IGunOperator");
            Class<?> reload = Class.forName(TACZ + "api.entity.ReloadState");
            return new GunState(operator.getMethod("getSynAimingProgress"), operator.getMethod("getSynReloadState"),
                    reload.getMethod("getStateType"), reload.getMethod("getCountDown"),
                    operator.getMethod("getSynDrawCoolDown"), operator.getMethod("getSynIsBolting"));
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
