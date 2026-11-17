execute unless score #ot cs2_bomb_state matches 1 run return 0
scoreboard players set #state cs2_state 6
scoreboard players set #break_type cs2_counts 3
scoreboard players set #ot_half cs2_bomb_state 1
scoreboard players set #timer cs2_timer 200
bossbar set cs2:round visible false
bossbar set cs2:bomb visible false
function cs2:join/swap
function cs2:round/swap_state
execute if entity @e[type=marker,tag=cs2_lobby] run tp @a[scores={cs2_team=1..2}] @e[type=marker,tag=cs2_lobby,limit=1]

title @a[scores={cs2_team=1..2}] times 10 60 10
title @a[scores={cs2_team=1..2,cs2_hud=1}] title {"text":"加时换边","color":"aqua","bold":true}
title @a[scores={cs2_team=1..2,cs2_hud=1}] subtitle {"text":"还剩 3 回合","color":"gray"}
tellraw @a[scores={cs2_team=1..2}] {"text":"加时赛换边，先到 16 分获胜。","color":"aqua"}
