# 按本回合登记的阵营传送；每条开场或重连路径只调用一次。
execute if score @s cs2_team matches 1 if entity @e[type=marker,tag=cs2_spawn_t,limit=1] run tp @s @e[type=marker,tag=cs2_spawn_t,limit=1]
execute if score @s cs2_team matches 2 if entity @e[type=marker,tag=cs2_spawn_ct,limit=1] run tp @s @e[type=marker,tag=cs2_spawn_ct,limit=1]
execute if score @s cs2_team matches 0 if entity @e[type=marker,tag=cs2_lobby,limit=1] run tp @s @e[type=marker,tag=cs2_lobby,limit=1]
