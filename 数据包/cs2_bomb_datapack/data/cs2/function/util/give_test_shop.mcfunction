# 已经有一颗就不再重复发（用 clear 0 精确计数，覆盖所有槽位）
execute store result score #has_shop cs2_counts run clear @s minecraft:emerald[minecraft:custom_data~{cs2_shop_item:1b}] 0
execute if score #has_shop cs2_counts matches 1.. run return 0
execute unless score #testshop cs2_testshop matches 1 run return 0
say [shop] 发放测试商店给 @s
give @s minecraft:emerald[minecraft:custom_data={cs2_shop_item:1b,cs2_shop_id:"test_main"},minecraft:item_name='{"text":"测试商店","color":"green"}',minecraft:lore=['{"text":"右键打开测试商店","color":"gray"}','{"text":"全部物品免费","color":"dark_gray"}'],minecraft:max_stack_size=1] 1
