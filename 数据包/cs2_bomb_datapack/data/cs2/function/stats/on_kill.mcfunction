# 击杀者统计：只统计比赛参赛者
execute unless score @s cs2_inmatch matches 1 run return 0
scoreboard players add @s cs2_kills 1
tag @s add cs2_recent_killer
scoreboard players set @s cs2_killer_timer 100
