# 只给正在参赛的 T / CT 玩家设置体型。
execute if score #state cs2_state matches 1..6 if score @s cs2_inmatch matches 1 if score @s cs2_team matches 1..2 run return run function cs2:player/apply_match_scale

# 赛后离线的玩家保留该标记，重新上线时一起恢复。
execute unless score @s cs2_match_scaled matches 1 run return 0
attribute @s minecraft:generic.scale base set 1
scoreboard players reset @s cs2_match_scaled
