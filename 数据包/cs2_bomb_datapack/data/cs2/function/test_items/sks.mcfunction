# SKS  $5000
scoreboard players set @s cs2_wclass 5
tag @s add cs2_shop_buying
scoreboard players set @s cs2_primary 0
function cs2:shop/clear_primary
give @s tacz:modern_kinetic_gun[minecraft:custom_data={cs2_primary_gun:1b,AttachmentEXTENDED_MAG:{components:{"cmagic:item_volume":0.2f,"minecraft:custom_data":{AttachmentId:"huanmeng:pp_762_mag"}},count:1,id:"tacz:attachment"},AttachmentSCOPE:{components:{"cmagic:item_volume":0.2f,"minecraft:custom_data":{AttachmentId:"tacz:scope_elcan_4x"}},count:1,id:"tacz:attachment"},GunCurrentAmmoCount:20,GunFireMode:"SEMI",GunId:"tacz:sks_tactical",HasBulletInBarrel:1b}]
scoreboard players set @s cs2_primary 1
tag @s remove cs2_shop_buying
