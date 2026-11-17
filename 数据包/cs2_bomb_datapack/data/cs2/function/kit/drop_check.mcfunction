# 每 tick 标记掉落的拆弹器；地面有拆弹器时，清掉 CT 的持有分数，等待拾取转移
execute as @e[type=item] if data entity @s Item.components."minecraft:custom_data".cs2_kit run tag @s add cs2_kit_item
execute if entity @e[type=item,tag=cs2_kit_item] run scoreboard players set @a[team=cs2_ct] cs2_kit 0
