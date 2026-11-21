package com.sandrone.cs2grenade;

import java.util.List;
import java.util.Optional;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

/**
 * 精确击杀播报：直接读取 DamageSource 的因果实体，不再依赖数据包的
 * “最近击杀者”全局标签，因此同 tick 连杀、同归于尽和投掷物击杀都不会错配。
 */
public final class KillFeedHandler {
    private static final ResourceLocation TACZ_BULLET = ResourceLocation.fromNamespaceAndPath("tacz", "bullet");
    private static final ResourceLocation TACZ_GUN = ResourceLocation.fromNamespaceAndPath("tacz", "modern_kinetic_gun");
    private static final ResourceLocation KILL_REWARD = ResourceLocation.fromNamespaceAndPath("cs2", "kill_reward");

    private KillFeedHandler() {
    }

    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity victimEntity = event.getEntity();
        if (!(victimEntity instanceof ServerPlayer victim)) {
            return;
        }
        DamageSource source = event.getSource();
        TaczHitAttribution.Hit gunHit = TaczHitAttribution.consume(victim, source);
        MinecraftServer server = victim.getServer();
        if (server == null || !isInMatch(server, victim)) {
            return;
        }

        int state = score(server, "cs2_state", "#state");
        if (state != 3 && state != 4) {
            return;
        }

        // 爆炸伤害把伤害者放在 directEntity（explosion(exploder, cause)），
        // getEntity() 取的是 causingEntity，这里两者都兼容
        Entity attackerEntity = source.getEntity() != null ? source.getEntity() : source.getDirectEntity();
        if (gunHit != null) {
            attackerEntity = gunHit.attacker();
        }
        boolean selfKill = attackerEntity == victim && isGrenadeDamage(source);
        ServerPlayer killer = attackerEntity instanceof ServerPlayer p && p != victim ? p : null;
        int victimTeam = team(server, victim);
        int killerTeam = killer == null ? 0 : team(server, killer);

        ItemStack weapon = source.getWeaponItem();
        ResourceLocation gunId = gunHit != null ? gunHit.gunId() : lethalGunId(source, weapon);
        Component message = buildMessage(source, victim, victimTeam, killer, killerTeam, selfKill,
                weaponLabel(source, weapon, gunId));
        for (ServerPlayer receiver : server.getPlayerList().getPlayers()) {
            if (isInMatch(server, receiver) || "cs2_cast".equals(teamName(receiver))) {
                receiver.sendSystemMessage(message);
            }
        }

        boolean playerKill = killer != null && isInMatch(server, killer) && (killerTeam == 1 || killerTeam == 2)
                && (victimTeam == 1 || victimTeam == 2);
        boolean enemyKill = playerKill && killerTeam != victimTeam;
        if (playerKill || attackerEntity == victim) {
            // 自伤和友伤只掉枪，敌方击杀额外掉落一件投掷物。
            String lootCommand = enemyKill ? "drop_enemy_loot" : "drop_death_gun";
            var commands = server.getCommands();
            if (commands.getDispatcher().findNode(List.of("cs2shop", lootCommand)) != null) {
                commands.performPrefixedCommand(
                        victim.createCommandSourceStack().withPermission(4).withSuppressedOutput(), "cs2shop " + lootCommand);
            }
        }

        if (enemyKill) {
            // 友伤保留播报，只有击杀敌方参赛者计入击杀统计。
            command(server, "scoreboard players add " + killer.getScoreboardName() + " cs2_kills 1");
            command(server, "scoreboard players add " + killer.getScoreboardName() + " cs2_round_kills 1");
            // 击杀奖金/CT 团队击杀仍由数据包计算；同队击杀不会获得奖金。
            String rewardCommand;
            if (isGrenadeDamage(source)) {

                String enemyTeam = victimTeam == 1 ? "cs2_ct" : "cs2_t";
                rewardCommand = "execute if entity @s[team=" + enemyTeam + "] run function cs2:econ/kill/reward_grenade";
            } else {
                CompoundTag reward = new CompoundTag();
                reward.putString("gun", gunId == null ? "" : gunId.toString());
                Entity bullet = gunHit != null ? gunHit.bullet() : source.getDirectEntity();
                if (TaczHitAttribution.isP90HollowPoint(gunId, bullet)) {
                    reward.putBoolean("p90_hollow_point", true);
                }
                server.getCommandStorage().set(KILL_REWARD, reward);
                rewardCommand = "function cs2:econ/kill/" + (victimTeam == 1 ? "on_t" : "on_ct");
            }
            server.getCommands().performPrefixedCommand(
                    killer.createCommandSourceStack().withPermission(4).withSuppressedOutput(),
                    rewardCommand
            );
        } else if (playerKill) {
            server.getCommands().performPrefixedCommand(
                    killer.createCommandSourceStack().withPermission(4).withSuppressedOutput(),
                    "function cs2:econ/kill/team_kill_penalty"
            );
        }
    }

    private static Component buildMessage(DamageSource source, ServerPlayer victim, int victimTeam,
                                          ServerPlayer killer, int killerTeam, boolean selfKill, Component weapon) {
        if (selfKill) {
            String deathVerb = source.is(GrenadeManager.FIRE_DAMAGE_TYPE) ? "烧死了" : "炸死了";
            MutableComponent result = Component.empty();
            result.append(victim.getDisplayName())
                    .append(Component.literal(" 被自己的").withStyle(ChatFormatting.GRAY))
                    .append(weapon != null ? weapon : Component.literal("投掷物").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal(deathVerb).withStyle(ChatFormatting.GRAY));
            return result;
        }
        if (killer == null) {
            return victim.getCombatTracker().getDeathMessage();
        }
        boolean teamKill = victimTeam == killerTeam && victimTeam != 0;
        MutableComponent result = Component.empty();
        if (teamKill) {
            result.append(Component.literal("[友伤] ").withStyle(ChatFormatting.DARK_RED));
        }
        result.append(teamTag(killerTeam))
                .append(killer.getDisplayName());
        if (weapon != null) {
            result.append(Component.literal(" [").withStyle(ChatFormatting.GRAY))
                    .append(weapon)
                    .append(Component.literal("] ").withStyle(ChatFormatting.GRAY));
        } else {
            result.append(Component.literal(" "));
        }
        result.append(Component.literal("击杀了 ").withStyle(ChatFormatting.GRAY))
                .append(teamTag(victimTeam))
                .append(victim.getDisplayName());
        return result;
    }

    private static ResourceLocation lethalGunId(DamageSource source, ItemStack weapon) {
        Entity direct = source.getDirectEntity();
        if (direct != null && TACZ_BULLET.equals(BuiltInRegistries.ENTITY_TYPE.getKey(direct.getType()))) {
            // TACZ stores the firing gun on the projectile. Keep TACZ optional for standalone builds.
            try {
                if (direct.getClass().getMethod("getGunId").invoke(direct) instanceof ResourceLocation gunId) {
                    return gunId;
                }
            } catch (ReflectiveOperationException ignored) {
                // Unknown TACZ versions fall back to the default reward.
            }
            return null;
        }
        if (weapon != null && TACZ_GUN.equals(BuiltInRegistries.ITEM.getKey(weapon.getItem()))) {
            CustomData data = weapon.get(DataComponents.CUSTOM_DATA);
            if (data != null) {
                String gunId = data.copyTag().getString("GunId");
                return gunId.isEmpty() ? null : ResourceLocation.tryParse(gunId);
            }
        }
        return null;
    }

    private static MutableComponent gunName(ResourceLocation gunId) {
        // The common index carries the translation key on a dedicated server.
        // Keep TaCZ optional and show the known ID when its index cannot supply a name.
        try {
            Class<?> api = Class.forName("com.tacz.guns.api.TimelessAPI");
            if (api.getMethod("getCommonGunIndex", ResourceLocation.class).invoke(null, gunId)
                    instanceof Optional<?> entry && entry.isPresent()) {
                Object index = entry.get();
                Object config = index.getClass().getMethod("getPojo").invoke(index);
                if (config != null && config.getClass().getMethod("getName").invoke(config)
                        instanceof String name && !name.isBlank()) {
                    return Component.translatableWithFallback(name, gunId.toString());
                }
            }
        } catch (ReflectiveOperationException | LinkageError ignored) {
            // A different TaCZ build can still have a usable projectile ID.
        }
        return Component.literal(gunId.toString());
    }

    private static boolean isGrenadeDamage(DamageSource source) {
        return source instanceof GrenadeExplosionDamageSource || source.is(GrenadeManager.FIRE_DAMAGE_TYPE);
    }

    private static Component weaponLabel(DamageSource source, ItemStack weapon, ResourceLocation gunId) {
        if (source instanceof GrenadeExplosionDamageSource) {
            return Component.literal("高爆手雷").withStyle(ChatFormatting.GOLD);
        }
        if (source.is(GrenadeManager.FIRE_DAMAGE_TYPE)) {
            return Component.literal("燃烧弹").withStyle(ChatFormatting.GOLD);
        }
        if (gunId != null) {
            return gunName(gunId).withStyle(ChatFormatting.YELLOW);
        }
        // 伤害源未记录武器时省略名称，避免把命中前换到手上的物品当成凶器。
        if (weapon == null || weapon.isEmpty()) {
            return null;
        }
        return weapon.getHoverName().copy().withStyle(ChatFormatting.YELLOW);
    }

    private static Component teamTag(int team) {
        return switch (team) {
            case 1 -> Component.literal("[T] ").withStyle(ChatFormatting.RED);
            case 2 -> Component.literal("[CT] ").withStyle(ChatFormatting.BLUE);
            default -> Component.literal("[?] ").withStyle(ChatFormatting.GRAY);
        };
    }

    private static boolean isInMatch(MinecraftServer server, ServerPlayer player) {
        return score(server, "cs2_inmatch", player.getScoreboardName()) == 1;
    }

    private static int team(MinecraftServer server, ServerPlayer player) {
        return score(server, "cs2_team", player.getScoreboardName());
    }

    private static String teamName(ServerPlayer player) {
        return player.getTeam() == null ? "" : player.getTeam().getName();
    }

    private static int score(MinecraftServer server, String objectiveName, String holderName) {
        Scoreboard scoreboard = server.getScoreboard();
        Objective objective = scoreboard.getObjective(objectiveName);
        if (objective == null) {
            return 0;
        }
        return scoreboard.getOrCreatePlayerScore(ScoreHolder.forNameOnly(holderName), objective).get();
    }

    private static void command(MinecraftServer server, String command) {
        server.getCommands().performPrefixedCommand(
                server.createCommandSourceStack().withPermission(4).withSuppressedOutput(),
                command
        );
    }
}
