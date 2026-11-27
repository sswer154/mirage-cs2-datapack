# 回合开始：冻结阶段
scoreboard players set #state cs2_state 2
execute as @a[predicate=cs2:match_audio] run function cs2:music/stop
function cs2:pvp/off
# 投票重开可以跳过回合结算，先清掉上一轮已安放的 C4。
kill @e[tag=cs2_bomb]
kill @e[type=item_display,tag=cs2_c4_display]
kill @e[type=armor_stand,tag=cs2_c4_hit]
scoreboard players set #bomb cs2_bomb_state 0
scoreboard players set #bomb_timer cs2_bomb_timer 0
scoreboard players set #site cs2_site 0
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
# 阵营和回合状态结算后，只发送一次出生点传送。
execute as @a[scores={cs2_inmatch=1}] run function cs2:player/teleport_round_spawn
function cs2:round/freeze
execute if score #round_replayed cs2_counts matches 1 as @a[scores={cs2_inmatch=1,cs2_team=1..2}] run function cs2:round/restore_checkpoint
execute as @a[scores={cs2_inmatch=1,cs2_team=1..2}] run cs2roundstate save
# 默认装备、商店与重开背包都结算后，再同步客户端显示。
execute as @a[scores={cs2_inmatch=1,cs2_team=1..2}] run cs2inventory sync
scoreboard players set #round_restarting cs2_counts 0
