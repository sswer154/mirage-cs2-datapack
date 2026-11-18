# 购买前先核对副武器分数：枪已经丢了就不该拦购买
function cs2:shop/reconcile_secondary
# CZ75  $300
scoreboard players set @s cs2_wclass 1
clear @s tacz:modern_kinetic_gun[minecraft:custom_data~{GunId:"tacz:glock_17"}]
clear @s tacz:modern_kinetic_gun[minecraft:custom_data~{GunId:"tacz:m9a4"}]
scoreboard players set @s cs2_secondary 1
give @s tacz:modern_kinetic_gun[minecraft:custom_data={cs2_secondary_gun:1b,AttachmentEXTENDED_MAG:{components:{"minecraft:custom_data":{AttachmentId:"huanmeng:n7n31_9x19_mag"}},count:1,id:"tacz:attachment"},GunCurrentAmmoCount:27,GunFireMode:"AUTO",GunId:"tacz:cz75",HasBulletInBarrel:1b}]
