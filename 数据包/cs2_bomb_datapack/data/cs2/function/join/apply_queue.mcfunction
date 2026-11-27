execute if score #solo cs2_counts matches 0 unless function cs2:fairplay/ready run return run function cs2:join/cancel_queue
# 重连的老队员可能错过了中场换边。先对齐，再判断是否还需要换队。
execute if score @s cs2_inmatch matches 1 run function cs2:join/sync_side
execute if score @s cs2_inmatch matches 1 if score @s cs2_team = @s cs2_queued_team run return run function cs2:join/clear_queue

execute unless score @s cs2_inmatch matches 1 run scoreboard players set @s cs2_kills 0
execute unless score @s cs2_inmatch matches 1 run scoreboard players reset @s cs2_deaths
scoreboard players operation @s cs2_team = @s cs2_queued_team
execute if score @s cs2_team matches 1 run team join cs2_t @s
execute if score @s cs2_team matches 2 run team join cs2_ct @s
scoreboard players operation @s cs2_side_epoch = #swap cs2_side_epoch
execute if score #transition cs2_formal_epoch matches 1.. run scoreboard players operation @s cs2_formal_epoch = #transition cs2_formal_epoch
scoreboard players set @s cs2_inmatch 1
scoreboard players set @s cs2_alive 0
scoreboard players set @s cs2_money 800
# 由回合开场或准备阶段重连流程初始化新阵营的装备与位置。
scoreboard players reset @s cs2_health_round
function cs2:join/clear_queue
execute if score @s cs2_team matches 1 run tellraw @s {"text":"等待结束，已加入 T。","color":"red"}
execute if score @s cs2_team matches 2 run tellraw @s {"text":"等待结束，已加入 CT。","color":"blue"}
