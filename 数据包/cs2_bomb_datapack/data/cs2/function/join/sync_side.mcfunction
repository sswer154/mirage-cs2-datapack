# 旧存档第一次遇到新流程时，按本场尚未记录换边处理。
scoreboard players add @s cs2_side_epoch 0
execute if score @s cs2_side_epoch = #swap cs2_side_epoch run return 0
scoreboard players operation #missed_swaps cs2_side_epoch = #swap cs2_side_epoch
scoreboard players operation #missed_swaps cs2_side_epoch -= @s cs2_side_epoch
scoreboard players operation #missed_swaps cs2_side_epoch %= #side_mod cs2_side_epoch
# 只在错过奇数次换边时翻转阵营（1 ↔ 2）。
scoreboard players set #side cs2_side_epoch 3
scoreboard players operation #side cs2_side_epoch -= @s cs2_team
execute if score #missed_swaps cs2_side_epoch matches 1 run scoreboard players operation @s cs2_team = #side cs2_side_epoch
execute if score @s cs2_team matches 1 run team join cs2_t @s
execute if score @s cs2_team matches 2 run team join cs2_ct @s
function cs2:player/tag
clear @s tacz:modern_kinetic_gun[minecraft:custom_data~{GunId:"tacz:glock_17"}]
clear @s tacz:modern_kinetic_gun[minecraft:custom_data~{GunId:"tacz:m9a4"}]
scoreboard players set @s cs2_secondary 0
scoreboard players set @s cs2_needs_default 1
execute if score @s cs2_side_epoch < #halftime cs2_side_epoch run function cs2:econ/halftime_player
scoreboard players operation @s cs2_side_epoch = #swap cs2_side_epoch
