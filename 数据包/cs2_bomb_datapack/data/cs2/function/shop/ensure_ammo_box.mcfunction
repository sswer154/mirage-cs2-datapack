# 副手被其他物品占用时保留原物品，也保留通过交换挪到背包里的弹药盒。
execute unless items entity @s weapon.offhand * run return run function cs2:shop/ammo_box_reset
execute unless items entity @s weapon.offhand tacz:ammo_box[minecraft:custom_data~{AllTypeCreative:1b}] run return 0
execute if items entity @s hotbar.* tacz:ammo_box[minecraft:custom_data~{AllTypeCreative:1b}] run return run function cs2:shop/ammo_box_reset
execute if items entity @s inventory.* tacz:ammo_box[minecraft:custom_data~{AllTypeCreative:1b}] run function cs2:shop/ammo_box_reset
