scoreboard players reset @s cs2_join_cast
scoreboard players enable @s cs2_join_cast
# 只有非参赛者才能转解说，避免从 T/CT 拉人影响回合人数/胜负
execute if score @s cs2_inmatch matches 1 run tellraw @s {"text":"你正在参加比赛，不能直接转为解说。请先结束/离开当前对局。","color":"red"}
execute if score @s cs2_inmatch matches 1 run return 0
execute if entity @s[team=cs2_t] run team leave @s
execute if entity @s[team=cs2_ct] run team leave @s
team join cs2_cast @s
scoreboard players set @s cs2_team 3
scoreboard players set @s cs2_alive 0
scoreboard players set @s cs2_carrier 0
scoreboard players set @s cs2_kit 0
scoreboard players set @s cs2_inmatch 0
scoreboard players set @s cs2_needs_default 0
scoreboard players set @s cs2_was_alive 0
function cs2:player/tag
clear @s minecraft:nautilus_shell[minecraft:custom_data={cs2_bomb:1b}]
clear @s minecraft:shears[minecraft:custom_data={cs2_kit:1b}]
effect clear @s minecraft:speed
effect give @s minecraft:night_vision infinite 0 true
effect give @s minecraft:invisibility infinite 0 true
function cs2:util/jump_unlock
gamemode spectator @s
cs2inventory restore
tellraw @s {"text":"已加入解说队伍：可查看击杀播报，不参与回合、不影响比分。","color":"aqua"}
tellraw @s {"text":"离开解说请使用 /trigger cs2_leave。","color":"gray"}
