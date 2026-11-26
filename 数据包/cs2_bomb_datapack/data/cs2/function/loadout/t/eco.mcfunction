# ===== T · ECO / 手枪局 =====
# TACZ Glock-17（17 发，半自动）+ 创造模式全种类弹药盒
item replace entity @s armor.head with minecraft:air
item replace entity @s armor.chest with minecraft:air
item replace entity @s armor.legs with minecraft:air
item replace entity @s armor.feet with minecraft:air
give @s tacz:modern_kinetic_gun[minecraft:custom_data={GunCurrentAmmoCount:17,GunFireMode:"SEMI",GunId:"tacz:glock_17",HasBulletInBarrel:1b}]
function cs2:shop/ensure_ammo_box
