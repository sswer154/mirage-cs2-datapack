# 刘易斯轻机枪  $4500
scoreboard players set @s cs2_wclass 6
tag @s add cs2_shop_buying
scoreboard players set @s cs2_primary 0
function cs2:shop/clear_primary
give @s tacz:modern_kinetic_gun[minecraft:custom_data={cs2_primary_gun:1b,AttachmentSCOPE:{components:{"minecraft:custom_data":{AttachmentId:"tacz:scope_hamr"}},count:1,id:"tacz:attachment"},AttachmentSTOCK:{components:{"minecraft:custom_data":{AttachmentId:"tacz:stock_hk_slim_line"}},count:1,id:"tacz:attachment"},GunCurrentAmmoCount:45,GunFireMode:"AUTO",GunId:"bf1:lewis",HasBulletInBarrel:1b}]
scoreboard players set @s cs2_primary 1
tag @s remove cs2_shop_buying
