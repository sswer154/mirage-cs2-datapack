package com.sandrone.cs2grenade;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

/**
 * 精确击杀播报：直接读取 DamageSource 的因果实体，不再依赖数据包的
 * “最近击杀者”全局标签，因此同 tick 连杀、同归于尽和投掷物击杀都不会错配。
 */
public final class KillFeedHandler {
    private KillFeedHandler() {
    }

    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity victimEntity = event.getEntity();
        if (!(victimEntity instanceof ServerPlayer victim)) {
            return;
        }
        MinecraftServer server = victim.getServer();
        if (server == null || !isInMatch(server, victim)) {
            return;
        }

        int state = score(server, "cs2_state", "#state");
        if (state != 3 && state != 4) {
            return;
        }

        DamageSource source = event.getSource();
        // 爆炸伤害把伤害者放在 directEntity（explosion(exploder, cause)），
        // getEntity() 取的是 causingEntity，这里两者都兼容
        Entity attackerEntity = source.getEntity() != null ? source.getEntity() : source.getDirectEntity();
        boolean selfKill = attackerEntity == victim && isGrenadeDamage(source);
        ServerPlayer killer = attackerEntity instanceof ServerPlayer p && p != victim ? p : null;
        int victimTeam = team(server, victim);
        int killerTeam = killer == null ? 0 : team(server, killer);

        Component message = buildMessage(source, victim, victimTeam, killer, killerTeam, selfKill);
        for (ServerPlayer receiver : server.getPlayerList().getPlayers()) {
            if (isInMatch(server, receiver) || "cs2_cast".equals(teamName(receiver))) {
                receiver.sendSystemMessage(message);
            }
        }

        if (killer != null && (killerTeam == 1 || killerTeam == 2) && (victimTeam == 1 || victimTeam == 2)) {
            // K/D 完全由模组维护；数据包旧 advancement 已停用，避免重复计数。
            command(server, "scoreboard players add " + killer.getScoreboardName() + " cs2_kills 1");
            if (killerTeam != victimTeam) {
                command(server, "scoreboard players add " + killer.getScoreboardName() + " cs2_round_kills 1");
            }
            // 击杀奖金/CT 团队击杀仍由数据包计算；同队击杀不会获得奖金。
            String fn = "cs2:econ/kill/" + (victimTeam == 1 ? "on_t" : "on_ct");
            server.getCommands().performPrefixedCommand(
                    killer.createCommandSourceStack().withPermission(4).withSuppressedOutput(),
                    "function " + fn
            );
        }
    }

    private static Component buildMessage(DamageSource source, ServerPlayer victim, int victimTeam, ServerPlayer killer, int killerTeam, boolean selfKill) {
        if (selfKill) {
            Component weapon = weaponLabel(source);
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
        Component weapon = weaponLabel(source);
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

    private static boolean isGrenadeDamage(DamageSource source) {
        String damageId = source.getMsgId();
        return damageId.startsWith("explosion") || source.is(GrenadeManager.FIRE_DAMAGE_TYPE);
    }

    private static Component weaponLabel(DamageSource source) {
        String damageId = source.getMsgId();
        if (damageId.startsWith("explosion")) {
            return Component.literal("高爆手雷").withStyle(ChatFormatting.GOLD);
        }
        if (source.is(GrenadeManager.FIRE_DAMAGE_TYPE)) {
            return Component.literal("燃烧弹").withStyle(ChatFormatting.GOLD);
        }
        ItemStack weapon = source.getWeaponItem();
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
