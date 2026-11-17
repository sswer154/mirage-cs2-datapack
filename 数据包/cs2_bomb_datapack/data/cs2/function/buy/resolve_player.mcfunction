# 解析单个玩家最终装备：合法选择优先，否则按默认顺序（长枪 -> ECO -> 强起 -> 狙击）
scoreboard players set @s cs2_buy 0
execute if score @s cs2_choice matches 1 if score @s cs2_allow_eco matches 1 run scoreboard players set @s cs2_buy 1
execute if score @s cs2_choice matches 2 if score @s cs2_allow_force matches 1 run scoreboard players set @s cs2_buy 2
execute if score @s cs2_choice matches 3 if score @s cs2_allow_rifle matches 1 run scoreboard players set @s cs2_buy 3
execute if score @s cs2_choice matches 4 if score @s cs2_allow_sniper matches 1 run scoreboard players set @s cs2_buy 4

execute if score @s cs2_buy matches 0 if score @s cs2_allow_rifle matches 1 run scoreboard players set @s cs2_buy 3
execute if score @s cs2_buy matches 0 if score @s cs2_allow_eco matches 1 run scoreboard players set @s cs2_buy 1
execute if score @s cs2_buy matches 0 if score @s cs2_allow_force matches 1 run scoreboard players set @s cs2_buy 2
execute if score @s cs2_buy matches 0 if score @s cs2_allow_sniper matches 1 run scoreboard players set @s cs2_buy 4
execute if score @s cs2_buy matches 0 run scoreboard players set @s cs2_buy 1