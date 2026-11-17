# 默认配发：Glock-17 + 零时大剑 + 创造弹药盒；购买状态清零
give @s tacz:modern_kinetic_gun[minecraft:custom_data={cs2_secondary_gun:1b,GunCurrentAmmoCount:17,GunFireMode:"SEMI",GunId:"tacz:glock_17",HasBulletInBarrel:1b}]
give @s tacz:modern_kinetic_gun[minecraft:custom_data={AttachmentEXTENDED_MAG:{components:{"minecraft:custom_data":{AttachmentId:"claymore:soulgem"}},count:1,id:"tacz:attachment"},AttachmentMUZZLE:{components:{"minecraft:custom_data":{AttachmentId:"claymore:heavyblade"}},count:1,id:"tacz:attachment"},GunCurrentAmmoCount:100,GunFireMode:"BURST",GunId:"claymore:zerotime",HasBulletInBarrel:1b}]
item replace entity @s weapon.offhand with tacz:ammo_box[minecraft:custom_data={AllTypeCreative:1b}]
scoreboard players set @s cs2_primary 0
scoreboard players set @s cs2_secondary 0
scoreboard players set @s cs2_armor 0
scoreboard players set @s cs2_kit 0
scoreboard players set @s cs2_planter 0
scoreboard players set @s cs2_wclass 1
