# C4 携带者阵亡：死亡不掉落，C4 回收后 5 秒补发给存活 T
scoreboard players set @s cs2_carrier 0
clear @s minecraft:nautilus_shell[minecraft:custom_data={cs2_bomb:1b}]
scoreboard players set #bomb cs2_bomb_state 2
scoreboard players set #drop_timer cs2_counts 100
title @s actionbar {"text":"C4 已回收，5 秒后补发","color":"red"}
