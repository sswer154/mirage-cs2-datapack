# 副武器分数与实际背包核对：分数为 1 但背包里没有副武器时清回 0，
# 让 ensure_default_* 在准备阶段补发；解决“丢枪后不补发、又买不了”的问题
execute unless score @s cs2_inmatch matches 1 run return 0
execute unless score @s cs2_secondary matches 1 run return 0
execute if items entity @s inventory.* tacz:modern_kinetic_gun[minecraft:custom_data~{cs2_secondary_gun:1b}] run return 0
execute if items entity @s hotbar.* tacz:modern_kinetic_gun[minecraft:custom_data~{cs2_secondary_gun:1b}] run return 0
execute if items entity @s weapon.offhand tacz:modern_kinetic_gun[minecraft:custom_data~{cs2_secondary_gun:1b}] run return 0
scoreboard players set @s cs2_secondary 0
