# CT 带着拆弹器阵亡：先清掉背包里的拆弹器；如果炸弹已下包且地面没有拆弹器，补发给存活 CT
clear @s minecraft:shears[minecraft:custom_data={cs2_kit:1b}]
scoreboard players set @s cs2_kit 0

# 标记所有掉在地上的拆弹器（keepInventory=false 时会自然掉落）
execute as @e[type=item] if data entity @s Item.components."minecraft:custom_data".cs2_kit run tag @s add cs2_kit_item

# 只有炸弹已安放、且地面没有拆弹器可捡时，才补发
execute if score #state cs2_state matches 4 if score #bomb cs2_bomb_state matches 3 unless entity @e[type=item,tag=cs2_kit_item] run function cs2:kit/reissue
