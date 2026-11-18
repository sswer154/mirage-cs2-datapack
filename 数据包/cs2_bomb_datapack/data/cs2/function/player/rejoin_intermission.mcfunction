# 回合结束/加时休息阶段重连：保留参赛者的装备/效果，只回大厅等待
execute unless score @s cs2_inmatch matches 1 run function cs2:player/make_spec
execute unless score @s cs2_inmatch matches 1 run return 0
execute if score @s cs2_team matches 0 run function cs2:join/to_lobby
execute if score @s cs2_team matches 0 run return 0

function cs2:util/clear_match_state
gamemode adventure @s
function cs2:util/speed
function cs2:player/tag
execute if entity @e[type=marker,tag=cs2_lobby] run tp @s @e[type=marker,tag=cs2_lobby,limit=1]
tellraw @s {"text":"已重新连接：保留当前装备与效果，等待下一回合。","color":"green"}
# 回合结束阶段重连：C4 清掉，下一回合重新发放
clear @s minecraft:nautilus_shell[minecraft:custom_data={cs2_bomb:1b}]
scoreboard players set @s cs2_carrier 0
