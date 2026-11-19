# 准备阶段重连：已参赛且已有装备的玩家保留背包/效果，只回出生点
execute unless score @s cs2_inmatch matches 1 run function cs2:join/freeze_join
execute unless score @s cs2_inmatch matches 1 run return 0
execute unless score @s cs2_needs_default matches 0 run function cs2:join/freeze_join
execute unless score @s cs2_needs_default matches 0 run return 0

function cs2:player/restore_health
gamemode adventure @s
function cs2:util/speed
# 准备阶段的移动锁是 slowness 255 + resistance 255，重连的人也要补上，
# 否则会带着 util/speed 的速度效果直接跑起来；round/live 会统一清掉这两个效果
effect give @s minecraft:slowness 30 255 true
effect give @s minecraft:resistance 30 255 true
function cs2:util/jump_lock
function cs2:player/tag
execute if score @s cs2_team matches 1 if entity @e[type=marker,tag=cs2_spawn_t,limit=1] run tp @s @e[type=marker,tag=cs2_spawn_t,limit=1]
execute if score @s cs2_team matches 2 if entity @e[type=marker,tag=cs2_spawn_ct,limit=1] run tp @s @e[type=marker,tag=cs2_spawn_ct,limit=1]
function cs2:shop/ensure_ready
tellraw @s {"text":"已重新连接：保留当前装备与效果，准备阶段继续。","color":"green"}
# 准备阶段重连：清掉背包里的 C4，交给下一回合随机发放
clear @s minecraft:nautilus_shell[minecraft:custom_data={cs2_bomb:1b}]
scoreboard players set @s cs2_carrier 0
