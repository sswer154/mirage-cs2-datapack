# 购买前先核对副武器分数：枪已经丢了就不该拦购买
function cs2:shop/reconcile_secondary
# 沙漠之鹰  $700
scoreboard players set @s cs2_wclass 1
clear @s tacz:modern_kinetic_gun[minecraft:custom_data~{GunId:"tacz:glock_17"}]
clear @s tacz:modern_kinetic_gun[minecraft:custom_data~{GunId:"tacz:m9a4"}]
scoreboard players set @s cs2_secondary 1
give @s tacz:modern_kinetic_gun[minecraft:custom_data={cs2_secondary_gun:1b,GunCurrentAmmoCount:7,GunFireMode:"SEMI",GunId:"tacz:deagle",HasBulletInBarrel:1b}]
