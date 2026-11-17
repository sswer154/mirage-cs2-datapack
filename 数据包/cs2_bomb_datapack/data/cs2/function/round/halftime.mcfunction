scoreboard players set #state cs2_state 6
scoreboard players set #break_type cs2_counts 1
scoreboard players set #half cs2_bomb_state 1
scoreboard players set #timer cs2_timer 200
bossbar set cs2:round visible false
bossbar set cs2:bomb visible false
function cs2:join/swap
function cs2:round/swap_state
function cs2:econ/halftime
execute if entity @e[type=marker,tag=cs2_lobby] run tp @a[scores={cs2_team=1..2}] @e[type=marker,tag=cs2_lobby,limit=1]

title @a[scores={cs2_team=1..2}] times 10 60 10
title @a[scores={cs2_team=1..2,cs2_hud=1}] title {"text":"换边","color":"aqua","bold":true}
title @a[scores={cs2_team=1..2,cs2_hud=1}] subtitle {"text":"T ↔ CT","color":"gray"}
tellraw @a[scores={cs2_team=1..2}] {"text":"上半场结束，双方换边；比分保留。","color":"aqua"}
playsound minecraft:block.note_block.chime master @a[scores={cs2_team=1..2}]
