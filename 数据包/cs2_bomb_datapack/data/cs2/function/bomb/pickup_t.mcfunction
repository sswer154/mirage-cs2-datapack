# T 拾取掉落炸弹
execute unless score #state cs2_state matches 3 run return 0
scoreboard players set @s cs2_carrier 1
scoreboard players set #bomb cs2_bomb_state 1
scoreboard players set #drop_timer cs2_counts 0
title @s actionbar {"text":"已拾取 C4","color":"red"}
playsound minecraft:entity.item.pickup master @s
function cs2:bomb/normalize_c4
