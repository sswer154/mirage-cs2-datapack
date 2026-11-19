# 回满血并清掉跨回合/跨重连残留的吸收
effect clear @s minecraft:absorption
effect give @s minecraft:instant_health 1 10 true
effect give @s minecraft:saturation 1 10 true
execute if score #epoch cs2_health_round matches 1.. run scoreboard players operation @s cs2_health_round = #epoch cs2_health_round
