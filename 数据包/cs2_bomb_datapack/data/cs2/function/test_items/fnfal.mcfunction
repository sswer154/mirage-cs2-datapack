# FN FAL  $3900
scoreboard players set @s cs2_wclass 3
tag @s add cs2_shop_buying
scoreboard players set @s cs2_primary 0
function cs2:shop/clear_primary
give @s tacz:modern_kinetic_gun[minecraft:custom_data={cs2_primary_gun:1b,AttachmentGRIP:{},AttachmentMUZZLE:{},AttachmentSCOPE:{components:{"minecraft:custom_data":{AttachmentId:"tacz:scope_hamr"}},count:1,id:"tacz:attachment"},GunCurrentAmmoCount:20,GunFireMode:"AUTO",GunId:"tacz:fn_fal",HasBulletInBarrel:1b}]
scoreboard players set @s cs2_primary 1
tag @s remove cs2_shop_buying
