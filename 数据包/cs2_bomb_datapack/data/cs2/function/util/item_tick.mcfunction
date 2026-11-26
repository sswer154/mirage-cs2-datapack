# 比赛掉落物清理（每 5 tick 调用一次）
# 只清理比赛发放且带 cs2_drop_clean 标记的装备；
# 场外普通的护甲/弹药盒没有标记，保留。
execute as @e[type=item,tag=!cs2_match_loot] if data entity @s Item.components."minecraft:custom_data".cs2_drop_clean run kill @s

# 商店道具（钻石/木棍/绿宝石）掉落即清
execute as @e[type=item] if data entity @s Item.components."minecraft:custom_data".cs2_shop_item run kill @s
