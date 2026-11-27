# 每 tick 同步体型，以覆盖重生或重连后重新创建的玩家实体。
attribute @s minecraft:generic.scale base set 0.937
execute unless score @s cs2_match_scaled matches 1 run scoreboard players set @s cs2_match_scaled 1
