execute if score #pause_t cs2_counts matches 3 run tellraw @s {"text":"T 队本场战术暂停已用完（3/3）。","color":"red"}
execute if score #pause_t cs2_counts matches 3 run return 0
scoreboard players add #pause_t cs2_counts 1
scoreboard players set #pause_side cs2_counts 1
function cs2:panel/pause_start
