# 比赛进行中重连：存活者保留背包/效果并回到生存；阵亡/非参赛者才转观战
execute if score @s cs2_inmatch matches 1 run function cs2:util/night_vision
execute if score @s cs2_inmatch matches 1 run function cs2:util/jump_unlock
execute unless score @s cs2_inmatch matches 1 run function cs2:player/make_spec
execute unless score @s cs2_inmatch matches 1 run return 0
execute unless score @s cs2_team matches 1..2 run function cs2:player/make_spec
execute unless score @s cs2_team matches 1..2 run return 0
execute unless score @s cs2_alive matches 1 run function cs2:player/make_spec
execute unless score @s cs2_alive matches 1 run return 0

function cs2:player/restore_health_if_due
gamemode survival @s
function cs2:util/speed
function cs2:player/tag
tellraw @s {"text":"已重新连接，比赛继续。","color":"green"}
# 正式回合重连：只保留一把 C4，有就从“发 C4”流程走一遍
scoreboard players set @s cs2_carrier 0
execute store result score #c4 cs2_counts run clear @s minecraft:nautilus_shell[minecraft:custom_data={cs2_bomb:1b}] 0
execute if score #c4 cs2_counts matches 1.. run function cs2:loadout/bomb_item
