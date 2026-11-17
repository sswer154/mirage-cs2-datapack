# 95式班用机枪  $4800
execute unless score #state cs2_state matches 2 run return 0
execute unless score @s cs2_team matches 1..2 run return 0
execute if score @s cs2_money matches ..4799 run tellraw @s "资金不足：需要 $4800。"
execute if score @s cs2_money matches ..4799 run return 0
scoreboard players remove @s cs2_money 4800
scoreboard players set @s cs2_wclass 6
tag @s add cs2_shop_buying
scoreboard players set @s cs2_primary 0
function cs2:shop/clear_primary
give @s tacz:modern_kinetic_gun[minecraft:custom_data={cs2_primary_gun:1b,AttachmentEXTENDED_MAG:{components:{"minecraft:custom_data":{AttachmentId:"tacz:extended_mag_3"}},count:1,id:"tacz:attachment"},AttachmentGRIP:{components:{"minecraft:custom_data":{AttachmentId:"tacz:grip_rk1_b25u"}},count:1,id:"tacz:attachment"},AttachmentSCOPE:{components:{"minecraft:custom_data":{AttachmentId:"tacz:scope_hamr"}},count:1,id:"tacz:attachment"},GunCurrentAmmoCount:75,GunFireMode:"AUTO",GunId:"tacz:qbz_95",HasBulletInBarrel:1b}]
scoreboard players set @s cs2_primary 1
tag @s remove cs2_shop_buying
tellraw @s [{"text":"已购买 ","color":"green"},{"text":"95式班用机枪","color":"yellow"},{"text":"  -$4800  余额 $","color":"gray"},{"score":{"name":"@s","objective":"cs2_money"},"color":"gold"}]
