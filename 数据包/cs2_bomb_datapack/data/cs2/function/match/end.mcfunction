# 比赛结束：按比分决定胜者（含平局），参赛者离队但不强制清包/传送
function cs2:practice/cleanup
scoreboard players set #state cs2_state 7
function cs2:panel/reset
gamerule keepInventory true
function cs2:pvp/on
bossbar set cs2:round visible false
bossbar set cs2:bomb visible false
kill @e[tag=cs2_bomb]
kill @e[type=item_display,tag=cs2_c4_display]
kill @e[type=armor_stand,tag=cs2_c4_hit]
kill @e[type=item,tag=cs2_util_projectile]
kill @e[type=marker,tag=cs2_smoke]
kill @e[type=marker,tag=cs2_fire]

# 结束比赛：不再清空背包/效果，也不再自动传送；只解除队伍
execute as @a[scores={cs2_inmatch=1}] run function cs2:util/clear_match_state

execute if score #t cs2_score_t > #ct cs2_score_ct run scoreboard players set #winner cs2_counts 1
execute if score #ct cs2_score_ct > #t cs2_score_t run scoreboard players set #winner cs2_counts 2
execute if score #t cs2_score_t = #ct cs2_score_ct run scoreboard players set #winner cs2_counts 0

execute if score #winner cs2_counts matches 1 run title @a[scores={cs2_inmatch=1,cs2_hud=1}] title {"text":"T 获胜","color":"red","bold":true}
execute if score #winner cs2_counts matches 2 run title @a[scores={cs2_inmatch=1,cs2_hud=1}] title {"text":"CT 获胜","color":"blue","bold":true}
execute if score #winner cs2_counts matches 0 run title @a[scores={cs2_inmatch=1,cs2_hud=1}] title {"text":"平局","color":"gray","bold":true}
title @a[scores={cs2_inmatch=1,cs2_hud=1}] subtitle {"text":"比赛结束","color":"gray"}
tellraw @a[scores={cs2_inmatch=1}] [{"text":"比赛结束  最终比分  T ","color":"gold"},{"score":{"name":"#t","objective":"cs2_score_t"}},{"text":" : ","color":"white"},{"score":{"name":"#ct","objective":"cs2_score_ct"}},{"text":" CT","color":"gold"}]
execute as @a[predicate=cs2:match_audio] at @s run playsound minecraft:ui.toast.challenge_complete master @s

# 结束前再发一次本场 K/D 与击杀王
function cs2:stats/match_end

# 参赛者先回大厅，再解除队伍；未参赛人员保持原地
execute if entity @e[type=marker,tag=cs2_lobby,limit=1] run tp @a[scores={cs2_inmatch=1}] @e[type=marker,tag=cs2_lobby,limit=1]
# 在大厅位置播放结算音乐，再清除参赛身份。
function cs2:music/play
execute as @a[scores={cs2_inmatch=1}] run function cs2:join/to_lobby
function cs2:join/end_queue
# 计分板仍保存离线参赛者的分数，比赛结束后也要撤销。
scoreboard players set * cs2_inmatch 0
