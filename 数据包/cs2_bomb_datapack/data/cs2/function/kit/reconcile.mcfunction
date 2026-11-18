# 拆弹器分数与实际背包核对：分数为 1 但身上没有钳子时清回 0；
# 有钳子就把分数补成 1，保证「拿在手上」和「拆包加速」一致
execute unless score @s cs2_kit matches 1 run return 0
execute if items entity @s inventory.* minecraft:shears[minecraft:custom_data~{cs2_kit:1b}] run return 0
execute if items entity @s hotbar.* minecraft:shears[minecraft:custom_data~{cs2_kit:1b}] run return 0
execute if items entity @s weapon.offhand minecraft:shears[minecraft:custom_data~{cs2_kit:1b}] run return 0
scoreboard players set @s cs2_kit 0
