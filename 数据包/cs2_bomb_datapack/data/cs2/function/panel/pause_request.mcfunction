scoreboard players reset @s cs2_pause
scoreboard players enable @s cs2_pause
execute if score #paused cs2_counts matches 1 run tellraw @s {"text":"当前已经在战术暂停中。","color":"red"}
execute if score #paused cs2_counts matches 1 run return 0
execute unless score #state cs2_state matches 2 run tellraw @s {"text":"只有准备阶段才能请求战术暂停。","color":"red"}
execute unless score #state cs2_state matches 2 run return 0
execute unless score @s cs2_inmatch matches 1 run tellraw @s {"text":"你不是本场参赛者。","color":"red"}
execute unless score @s cs2_inmatch matches 1 run return 0
execute if score @s cs2_team matches 1 run function cs2:panel/pause_t
execute if score @s cs2_team matches 2 run function cs2:panel/pause_c
