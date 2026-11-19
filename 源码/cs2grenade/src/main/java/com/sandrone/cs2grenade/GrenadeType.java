package com.sandrone.cs2grenade;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

/**
 * 一种投掷物的参数，来自数据包 data/&lt;命名空间&gt;/grenade/&lt;id&gt;.json。
 * 缺字段就用默认值，所以配置可以只写想改的几项。
 */
public record GrenadeType(
        ResourceLocation id,
        String effect,
        int cloudCount,
        double cloudRadius,
        int cloudSeconds,
        String cloudParticle,
        String maskColor,
        int maskAlpha,
        int maskSeconds,
        int modelCmd,
        int displayCmd,
        double renderOffsetY,
        double inheritVelocity,
        String hitSound,
        String hitSoundWall,
        String hitSoundTail,
        String hitSoundHead,
        String hitSoundShort,
        double speed,
        double gravity,
        double drag,
        double bounce,
        double bounceWall,
        int fuse,
        double radius,
        float damage,
        float knockback,
        float boomVolume,
        float boomPitch,
        float hitVolume,
        float hitVolumeWall,
        float hitPitch,
        float hitPitchWall,
        double hitMinSpeed,
        int fragRays,
        float fragDamage,
        float fragMinDamage,
        double fragInner,
        double fragOuter,
        float pierceDamage,
        double pierceInner,
        double pierceOuter
) {

    /** 找不到配置时的兜底，就是现在这套手感。 */
    public static GrenadeType fallback(ResourceLocation id) {
        return new GrenadeType(id, "he", 10, 2.5, 15, "minecraft:campfire_signal_smoke", "FFFFFF", 255, 1, 722002, 0, 0.35, 0.8, "minecraft:block.metal.place", "minecraft:block.metal.hit", "minecraft:block.metal.place", "minecraft:block.metal.place", "minecraft:block.metal.place", 1.45, 0.045, 0.99, 0.4, 0.5, 80,
                6.0, 24.0f, 0.6f, 6.0f, 1.0f, 0.9f, 0.35f, 1.35f, 1.0f, 0.12,
                20, 4.0f, 3.0f, 4.0, 15.0, 10.0f, 3.5, 6.0);
    }

    public static GrenadeType fromJson(ResourceLocation id, JsonObject o) {
        GrenadeType d = fallback(id);
        // 落地和撞墙共用 bounce，撞墙默认再脆一点（1.25 倍），需要时用 bounce_wall 单独覆盖
        double bounce = getDouble(o, "bounce", d.bounce);
        double bounceWall = getDouble(o, "bounce_wall", bounce * 1.25);
        return new GrenadeType(
                id,
                o.has("effect") ? o.get("effect").getAsString() : d.effect,
                getInt(o, "cloud_count", d.cloudCount),
                getDouble(o, "cloud_radius", d.cloudRadius),
                getInt(o, "cloud_seconds", d.cloudSeconds),
                o.has("cloud_particle") ? o.get("cloud_particle").getAsString() : d.cloudParticle,
                o.has("mask_color") ? o.get("mask_color").getAsString() : d.maskColor,
                getInt(o, "mask_alpha", d.maskAlpha),
                getInt(o, "mask_seconds", d.maskSeconds),
                getInt(o, "model_cmd", d.modelCmd),
                getInt(o, "display_cmd", d.displayCmd),
                getDouble(o, "render_offset_y", d.renderOffsetY),
                getDouble(o, "inherit_velocity", d.inheritVelocity),
                o.has("hit_sound") ? o.get("hit_sound").getAsString() : d.hitSound,
                o.has("hit_sound_wall") ? o.get("hit_sound_wall").getAsString() : d.hitSoundWall,
                o.has("hit_sound_tail") ? o.get("hit_sound_tail").getAsString() : d.hitSoundTail,
                o.has("hit_sound_short") ? o.get("hit_sound_short").getAsString() : d.hitSoundShort,
                o.has("hit_sound_head") ? o.get("hit_sound_head").getAsString() : d.hitSoundHead,
                getDouble(o, "speed", d.speed),
                getDouble(o, "gravity", d.gravity),
                getDouble(o, "drag", d.drag),
                bounce,
                bounceWall,
                getInt(o, "fuse", d.fuse),
                getDouble(o, "radius", d.radius),
                (float) getDouble(o, "damage", d.damage),
                (float) getDouble(o, "knockback", d.knockback),
                (float) getDouble(o, "boom_volume", d.boomVolume),
                (float) getDouble(o, "boom_pitch", d.boomPitch),
                (float) getDouble(o, "hit_volume", d.hitVolume),
                (float) getDouble(o, "hit_volume_wall", d.hitVolumeWall),
                (float) getDouble(o, "hit_pitch", d.hitPitch),
                (float) getDouble(o, "hit_pitch_wall", d.hitPitchWall),
                getDouble(o, "hit_min_speed", d.hitMinSpeed),
                getInt(o, "frag_rays", d.fragRays),
                (float) getDouble(o, "frag_damage", d.fragDamage),
                (float) getDouble(o, "frag_min_damage", d.fragMinDamage),
                getDouble(o, "frag_inner", d.fragInner),
                getDouble(o, "frag_outer", d.fragOuter),
                (float) getDouble(o, "pierce_damage", d.pierceDamage),
                getDouble(o, "pierce_inner", d.pierceInner),
                getDouble(o, "pierce_outer", d.pierceOuter)
        );
    }

    private static int getInt(JsonObject o, String key, int def) {
        return o.has(key) ? o.get(key).getAsInt() : def;
    }

    private static double getDouble(JsonObject o, String key, double def) {
        return o.has(key) ? o.get(key).getAsDouble() : def;
    }
}
