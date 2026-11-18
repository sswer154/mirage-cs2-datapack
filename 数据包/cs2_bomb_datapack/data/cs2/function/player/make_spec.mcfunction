# 变成观战：不再加入旁观队伍；保留 cs2_team，下一回合仍可回原队
# 重连到已结束的比赛时，确保不残留准备阶段的减速/锁跳
effect clear @s minecraft:slowness
effect clear @s minecraft:resistance
function cs2:util/jump_unlock
scoreboard players set @s cs2_alive 0
scoreboard players set @s cs2_carrier 0
scoreboard players set @s cs2_defuse 0
gamemode spectator @s
