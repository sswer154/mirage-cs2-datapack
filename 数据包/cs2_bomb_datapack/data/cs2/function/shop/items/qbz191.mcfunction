# 191式突击步枪  $3500
execute unless score #state cs2_state matches 2 run return 0
execute unless score @s cs2_team matches 1..2 run return 0
execute if score @s cs2_money matches ..3499 run tellraw @s "资金不足：需要 $3500。"
execute if score @s cs2_money matches ..3499 run return 0
scoreboard players remove @s cs2_money 3500
scoreboard players set @s cs2_wclass 3
tag @s add cs2_shop_buying
scoreboard players set @s cs2_primary 0
function cs2:shop/clear_primary
give @s tacz:modern_kinetic_gun[minecraft:custom_data={cs2_primary_gun:1b,AttachmentEXTENDED_MAG:{components:{"minecraft:custom_data":{AttachmentId:"huanmeng:dbp191_58_mag"}},count:1,id:"tacz:attachment"},AttachmentGRIP:{components:{"minecraft:custom_data":{AttachmentId:"tacz:grip_vertical_ranger"}},count:1,id:"tacz:attachment"},AttachmentMUZZLE:{components:{"minecraft:custom_data":{AttachmentId:"tacz:muzzle_brake_cthulhu"}},count:1,id:"tacz:attachment"},AttachmentSCOPE:{components:{"minecraft:custom_data":{AttachmentId:"tacz:scope_hamr"}},count:1,id:"tacz:attachment"},AttachmentSTOCK:{components:{"minecraft:custom_data":{AttachmentId:"tacz:stock_carbon_bone_c5"}},count:1,id:"tacz:attachment"},GunCurrentAmmoCount:30,GunFireMode:"AUTO",GunId:"tacz:qbz_191",HasBulletInBarrel:1b}]
scoreboard players set @s cs2_primary 1
tag @s remove cs2_shop_buying
tellraw @s [{"text":"已购买 ","color":"green"},{"text":"191式突击步枪","color":"yellow"},{"text":"  -$3500  余额 $","color":"gray"},{"score":{"name":"@s","objective":"cs2_money"},"color":"gold"}]
