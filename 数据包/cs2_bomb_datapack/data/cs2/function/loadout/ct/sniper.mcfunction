# ===== CT · 狙击 =====
# TACZ AWP（5 发，半自动）+ 8x 标准镜 + 全套铁甲 + 创造模式全种类弹药盒
item replace entity @s armor.head with minecraft:iron_helmet
item replace entity @s armor.chest with minecraft:iron_chestplate
item replace entity @s armor.legs with minecraft:iron_leggings
item replace entity @s armor.feet with minecraft:iron_boots
give @s tacz:modern_kinetic_gun[minecraft:custom_data={AttachmentSCOPE:{components:{"minecraft:custom_data":{AttachmentId:"tacz:scope_standard_8x",ZoomNumber:4}},count:1,id:"tacz:attachment"},GunCurrentAmmoCount:5,GunFireMode:"SEMI",GunId:"tacz:ai_awp",HasBulletInBarrel:1b}]
item replace entity @s weapon.offhand with tacz:ammo_box[minecraft:custom_data={AllTypeCreative:1b}]
