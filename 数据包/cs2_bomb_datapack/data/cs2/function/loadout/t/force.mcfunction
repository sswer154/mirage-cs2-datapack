# ===== T · 强起 =====
# TACZ MP5A5（29 发，全自动）+ 创造模式全种类弹药盒
item replace entity @s armor.head with minecraft:air
item replace entity @s armor.chest with minecraft:air
item replace entity @s armor.legs with minecraft:air
item replace entity @s armor.feet with minecraft:air
give @s tacz:modern_kinetic_gun[minecraft:custom_data={GunCurrentAmmoCount:30,GunFireMode:"AUTO",GunId:"tacz:hk_mp5a5",HasBulletInBarrel:1b}]
function cs2:shop/ensure_ammo_box
