# 统计背包里各类道具数量，供购买上限判断使用（clear 0 只计数不删除）
scoreboard players set #util_total cs2_counts 0
execute store result score #util_he cs2_counts run clear @s minecraft:snowball[minecraft:custom_data~{cs2_util:"he"}] 0
scoreboard players operation #util_total cs2_counts += #util_he cs2_counts
execute store result score #util_he_fast cs2_counts run clear @s minecraft:snowball[minecraft:custom_data~{cs2_util:"he_fast"}] 0
scoreboard players operation #util_total cs2_counts += #util_he_fast cs2_counts
execute store result score #util_flash cs2_counts run clear @s minecraft:snowball[minecraft:custom_data~{cs2_util:"flash"}] 0
scoreboard players operation #util_total cs2_counts += #util_flash cs2_counts
execute store result score #util_smoke cs2_counts run clear @s minecraft:snowball[minecraft:custom_data~{cs2_util:"smoke"}] 0
scoreboard players operation #util_total cs2_counts += #util_smoke cs2_counts
execute store result score #util_fire cs2_counts run clear @s minecraft:snowball[minecraft:custom_data~{cs2_util:"fire"}] 0
scoreboard players operation #util_total cs2_counts += #util_fire cs2_counts
execute store result score #util_impact cs2_counts run clear @s minecraft:snowball[minecraft:custom_data~{cs2_util:"impact"}] 0
scoreboard players operation #util_total cs2_counts += #util_impact cs2_counts
execute store result score #util_surgery cs2_counts run clear @s minecraft:paper[minecraft:custom_data~{cs2_util:"surgery_kit"}] 0
scoreboard players operation #util_total cs2_counts += #util_surgery cs2_counts
execute store result score #util_golden cs2_counts run clear @s minecraft:paper[minecraft:custom_data~{cs2_util:"golden_star"}] 0
scoreboard players operation #util_total cs2_counts += #util_golden cs2_counts
