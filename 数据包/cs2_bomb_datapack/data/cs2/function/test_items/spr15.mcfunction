# SPR-15 HB  $4800
scoreboard players set @s cs2_wclass 5
tag @s add cs2_shop_buying
scoreboard players set @s cs2_primary 0
function cs2:shop/clear_primary
give @s tacz:modern_kinetic_gun[minecraft:custom_data={cs2_primary_gun:1b,AttachmentSCOPE:{components:{"minecraft:custom_data":{AttachmentId:"tacz:scope_contender"}},count:1,id:"tacz:attachment"},GunCurrentAmmoCount:12,GunFireMode:"SEMI",GunId:"tacz:spr15hb",HasBulletInBarrel:1b}]
scoreboard players set @s cs2_primary 1
tag @s remove cs2_shop_buying
