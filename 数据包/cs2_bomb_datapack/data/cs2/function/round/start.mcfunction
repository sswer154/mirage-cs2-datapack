# 回合开始：冻结阶段
scoreboard players set #state cs2_state 2
function cs2:pvp/off
scoreboard players set #bomb cs2_bomb_state 0
scoreboard players set #bomb_timer cs2_bomb_timer 0
scoreboard players set #site cs2_site 0
scoreboard players set #drop_timer cs2_counts 0
scoreboard players set #plant_disadvantage cs2_counts 0
tag @a remove cs2_last_defuser
scoreboard players set #blink cs2_blink 0
scoreboard players set #blink_on cs2_blink 0
function cs2:map/doors/reset
function cs2:panel/reset_votes

scoreboard players add #epoch cs2_health_round 1
execute unless score #round_restarting cs2_counts matches 1 run scoreboard players operation #checkpoint cs2_health_round = #epoch cs2_health_round
scoreboard players set #round_replayed cs2_counts 0
execute if score #round_restarting cs2_counts matches 1 run scoreboard players set #round_replayed cs2_counts 1
execute unless score #round_restarting cs2_counts matches 1 as @a[scores={cs2_queued_team=1..2}] run function cs2:join/apply_queue
function cs2:econ/round_start
function cs2:player/round_all
tp @a[scores={cs2_inmatch=1,cs2_team=1}] @e[type=marker,tag=cs2_spawn_t,limit=1]
tp @a[scores={cs2_inmatch=1,cs2_team=2}] @e[type=marker,tag=cs2_spawn_ct,limit=1]
tp @a[scores={cs2_inmatch=1,cs2_team=0}] @e[type=marker,tag=cs2_lobby,limit=1]
function cs2:round/freeze
execute if score #round_replayed cs2_counts matches 1 as @a[scores={cs2_inmatch=1,cs2_team=1..2}] run function cs2:round/restore_checkpoint
execute as @a[scores={cs2_inmatch=1,cs2_team=1..2}] run cs2roundstate save
# 默认装备、商店与重开背包都结算后，再同步客户端显示。
execute as @a[scores={cs2_inmatch=1,cs2_team=1..2}] run cs2inventory sync
scoreboard players set #round_restarting cs2_counts 0
