# 死亡掉落规则
# 副武器、弹药盒、零时大剑：掉落即清除，不能拾取
execute as @e[type=item] if data entity @s Item.components."minecraft:custom_data".cs2_secondary_gun run kill @s
execute as @e[type=item] if data entity @s {Item:{id:"tacz:ammo_box"}} run kill @s
execute as @e[type=item] if data entity @s {Item:{components:{"minecraft:custom_data":{GunId:"claymore:zerotime"}}}} run kill @s

# 护甲：死亡掉落后直接清除，不能拾取
execute as @e[type=item] if data entity @s {Item:{id:"minecraft:netherite_helmet"}} run kill @s
execute as @e[type=item] if data entity @s {Item:{id:"minecraft:netherite_chestplate"}} run kill @s
execute as @e[type=item] if data entity @s {Item:{id:"minecraft:netherite_leggings"}} run kill @s
execute as @e[type=item] if data entity @s {Item:{id:"minecraft:netherite_boots"}} run kill @s
