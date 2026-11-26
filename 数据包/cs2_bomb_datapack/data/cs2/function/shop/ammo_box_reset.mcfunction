# 只在副手为空或已经是对局弹药盒时去重并放回一个。
execute if items entity @s weapon.offhand * unless items entity @s weapon.offhand tacz:ammo_box[minecraft:custom_data~{AllTypeCreative:1b}] run return 0
clear @s tacz:ammo_box[minecraft:custom_data~{AllTypeCreative:1b}]
item replace entity @s weapon.offhand with tacz:ammo_box[minecraft:custom_data={AllTypeCreative:1b,cs2_drop_clean:1b}]
