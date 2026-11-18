# 每 tick 标记掉落的拆弹器，供拾取与补发判断使用
execute as @e[type=item] if data entity @s Item.components."minecraft:custom_data".cs2_kit run tag @s add cs2_kit_item

# 不再按「地上有钳子」把全场分数清零：那会让刚补发到手上的钳子失效。
# 改为逐个核对：分数是 1 但身上没有钳子的，才清回 0
execute as @a[team=cs2_ct,scores={cs2_kit=1}] run function cs2:kit/reconcile
