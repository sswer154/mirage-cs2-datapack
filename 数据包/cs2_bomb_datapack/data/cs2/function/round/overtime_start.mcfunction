scoreboard players set #state cs2_state 6
scoreboard players set #break_type cs2_counts 2
scoreboard players set #ot cs2_bomb_state 1
scoreboard players set #ot_half cs2_bomb_state 0
scoreboard players operation #target cs2_score_t = #t cs2_score_t
scoreboard players add #target cs2_score_t 4
scoreboard players set #timer cs2_timer 200
bossbar set cs2:round visible false
bossbar set cs2:bomb visible false
function cs2:join/swap
function cs2:round/swap_state
execute if entity @e[type=marker,tag=cs2_lobby] run tp @a[scores={cs2_team=1..2}] @e[type=marker,tag=cs2_lobby,limit=1]

title @a[scores={cs2_team=1..2}] times 10 60 10
title @a[scores={cs2_team=1..2,cs2_hud=1}] title {"text":"加时赛","color":"gold","bold":true}
title @a[scores={cs2_team=1..2,cs2_hud=1}] subtitle {"text":"本组先赢 4 回合获胜","color":"gray"}
tellraw @a[scores={cs2_team=1..2}] [{"text":"进入新一组加时赛。先到 ","color":"gold"},{"score":{"name":"#target","objective":"cs2_score_t"}},{"text":" 分获胜，本组 3:3 则继续加时。"}]
playsound minecraft:block.note_block.chime master @a[scores={cs2_team=1..2}]
