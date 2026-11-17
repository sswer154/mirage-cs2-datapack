execute unless score @s cs2_carrier matches 1 run return 0
scoreboard players set @s cs2_carrier 0
scoreboard players set #bomb cs2_bomb_state 2
scoreboard players set #drop_timer cs2_counts 100
title @s actionbar {"text":"C4 已回收，5 秒后补发","color":"red"}
