# M1895  $2550
scoreboard players set @s cs2_wclass 7
tag @s add cs2_shop_buying
scoreboard players set @s cs2_primary 0
function cs2:shop/clear_primary
give @s tacz:modern_kinetic_gun[minecraft:custom_data={cs2_primary_gun:1b,AttachmentSCOPE:{components:{"minecraft:custom_data":{AttachmentId:"tacz:scope_elcan_4x"}},count:1,id:"tacz:attachment"},GunCurrentAmmoCount:4,GunFireMode:"SEMI",GunId:"bf1:man_m95",HasBulletInBarrel:1b}]
scoreboard players set @s cs2_primary 1
tag @s remove cs2_shop_buying
