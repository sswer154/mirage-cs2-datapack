# 败方玩家：
# 第 1 回合（手枪局）败者 -> ECO/强起；
# ECO 败者 -> 长枪（按规则，eco 存钱）；
# 强起败者 -> 只能 ECO；
# 长枪/狙击败者 -> ECO 或强起。
scoreboard players set @s cs2_allow_eco 0
scoreboard players set @s cs2_allow_force 0
scoreboard players set @s cs2_allow_rifle 0
scoreboard players set @s cs2_allow_sniper 0

execute if score #round cs2_round matches 1 run scoreboard players set @s cs2_allow_eco 1
execute if score #round cs2_round matches 1 run scoreboard players set @s cs2_allow_force 1
execute if score #round cs2_round matches 1 run return 0

execute if score @s cs2_buy matches 2 run scoreboard players set @s cs2_allow_eco 1
execute if score @s cs2_buy matches 2 run return 0

execute if score @s cs2_buy matches 3..4 run scoreboard players set @s cs2_allow_eco 1
execute if score @s cs2_buy matches 3..4 run scoreboard players set @s cs2_allow_force 1
execute if score @s cs2_buy matches 3..4 run return 0

execute if score @s cs2_buy matches 1 run scoreboard players set @s cs2_allow_rifle 1
execute if score @s cs2_buy matches 1 run return 0

# 兜底：ECO/强起二选一
scoreboard players set @s cs2_allow_eco 1
scoreboard players set @s cs2_allow_force 1