# P90（空尖弹）  $1500
scoreboard players set @s cs2_wclass 2
tag @s add cs2_shop_buying
scoreboard players set @s cs2_primary 0
function cs2:shop/clear_primary
give @s tacz:modern_kinetic_gun[minecraft:custom_data={cs2_primary_gun:1b,AttachmentLASER:{components:{"minecraft:custom_data":{AttachmentId:"huanmeng:ss197sr_57_laser"}},count:1,id:"tacz:attachment"},GunCurrentAmmoCount:50,GunFireMode:"AUTO",GunId:"tacz:p90",HasBulletInBarrel:1b}]
scoreboard players set @s cs2_primary 1
tag @s remove cs2_shop_buying
