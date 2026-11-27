# 每名玩家首次进入时初始化
scoreboard players set @s cs2_init 1
scoreboard players set @s cs2_team 0
scoreboard players set @s cs2_alive 0
scoreboard players set @s cs2_carrier 0
scoreboard players set @s cs2_kit 0
scoreboard players set @s cs2_defuse 0
scoreboard players set @s cs2_hud 1
scoreboard players set @s cs2_money 0
scoreboard players set @s cs2_primary 0
scoreboard players set @s cs2_secondary 0
scoreboard players set @s cs2_armor 0
scoreboard players set @s cs2_planter 0
scoreboard players set @s cs2_util_smoke 0
scoreboard players set @s cs2_util_he 0
scoreboard players set @s cs2_util_flash 0
scoreboard players set @s cs2_util_fire 0
scoreboard players set @s cs2_wclass 1
scoreboard players set @s cs2_inmatch 0
scoreboard players set @s cs2_needs_default 1
scoreboard players set @s cs2_was_alive 0
scoreboard players set @s cs2_kills 0
scoreboard players set @s cs2_round_kills 0
scoreboard players set @s cs2_fire_dmg 0
scoreboard players set @s cs2_grenade_dmg 0
scoreboard players set @s cs2_death_pending 0
scoreboard players set @s cs2_killer_timer 0
scoreboard players operation @s cs2_seen = #tick cs2_const
scoreboard players operation @s cs2_prev_deaths = @s cs2_deaths
# 对齐 sneak_time 统计，避免把进入前的累计值当成正在潜行
scoreboard players operation @s cs2_sneak_prev = @s cs2_sneak
scoreboard players enable @s cs2_join_t
scoreboard players enable @s cs2_join_ct
scoreboard players enable @s cs2_join_cast
scoreboard players enable @s cs2_leave
scoreboard players enable @s cs2_hud_toggle
scoreboard players enable @s cs2_stats
function cs2:panel/enable_triggers
function cs2:player/tag
tellraw @s {"text":"欢迎！按加入按钮，或用 /trigger cs2_join_t / cs2_join_ct 加入比赛；解说用 /trigger cs2_join_cast。","color":"gold"}
function cs2:player/on_join