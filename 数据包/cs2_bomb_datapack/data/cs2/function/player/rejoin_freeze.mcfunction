# 准备阶段重连：已参赛且已有装备的玩家保留背包/效果，只回出生点
execute if score @s cs2_inmatch matches 1 if score @s cs2_health_round = #epoch cs2_health_round unless score @s cs2_alive matches 1 run return run function cs2:player/make_spec
# 补入队时由 freeze_join 完成传送和准备状态。
execute unless score @s cs2_inmatch matches 1 run return run function cs2:join/freeze_join
execute unless score @s cs2_needs_default matches 0 run return run function cs2:join/freeze_join

execute if score @s cs2_alive matches 1 run function cs2:player/restore_health_if_due
gamemode adventure @s
function cs2:util/speed
function cs2:util/freeze_lock
function cs2:player/tag
execute if score @s cs2_team matches 1..2 run function cs2:player/teleport_round_spawn
function cs2:shop/ensure_ready
tellraw @s {"text":"已重新连接，准备阶段继续。","color":"green"}
# 准备阶段重连：清掉背包里的 C4，交给下一回合随机发放
clear @s minecraft:nautilus_shell[minecraft:custom_data={cs2_bomb:1b}]
scoreboard players set @s cs2_carrier 0
