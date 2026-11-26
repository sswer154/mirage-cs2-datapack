# ===== CT · 长枪 =====
# TACZ M4A1（29 发，全自动）+ 全套铁甲 + 创造模式全种类弹药盒
item replace entity @s armor.head with minecraft:iron_helmet
item replace entity @s armor.chest with minecraft:iron_chestplate
item replace entity @s armor.legs with minecraft:iron_leggings
item replace entity @s armor.feet with minecraft:iron_boots
give @s tacz:modern_kinetic_gun[minecraft:custom_data={GunCurrentAmmoCount:30,GunFireMode:"AUTO",GunId:"tacz:m4a1",HasBulletInBarrel:1b}]
function cs2:shop/ensure_ammo_box
