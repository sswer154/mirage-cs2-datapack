# M2火焰喷射器  $4800
scoreboard players set @s cs2_wclass 4
tag @s add cs2_shop_buying
scoreboard players set @s cs2_primary 0
function cs2:shop/clear_primary
give @s tacz:modern_kinetic_gun[minecraft:custom_data={cs2_primary_gun:1b,GunCurrentAmmoCount:240,GunFireMode:"AUTO",GunId:"bf1:m2_2",HasBulletInBarrel:1b,HeatAmount:0.0f}]
scoreboard players set @s cs2_primary 1
tag @s remove cs2_shop_buying
