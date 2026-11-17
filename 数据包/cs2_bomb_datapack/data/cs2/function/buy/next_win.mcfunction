# 胜方玩家：下一回合至少长枪；如果本回合已经是长枪/狙击，则可选狙击。
scoreboard players set @s cs2_allow_eco 0
scoreboard players set @s cs2_allow_force 0
scoreboard players set @s cs2_allow_rifle 0
scoreboard players set @s cs2_allow_sniper 0
scoreboard players set @s cs2_allow_rifle 1
execute if score @s cs2_buy matches 3..4 run scoreboard players set @s cs2_allow_sniper 1