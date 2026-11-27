# ===== CT · 强起 =====
# TACZ Vector .45（20 发，全自动）+ 创造模式全种类弹药盒
item replace entity @s armor.head with minecraft:air
item replace entity @s armor.chest with minecraft:air
item replace entity @s armor.legs with minecraft:air
item replace entity @s armor.feet with minecraft:air
give @s tacz:modern_kinetic_gun[minecraft:custom_data={GunCurrentAmmoCount:20,GunFireMode:"AUTO",GunId:"tacz:vector45",HasBulletInBarrel:1b}]
function cs2:shop/ensure_ammo_box
