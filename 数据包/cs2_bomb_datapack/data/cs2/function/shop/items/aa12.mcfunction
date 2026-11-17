# AA12  $2250
execute unless score #state cs2_state matches 2 run return 0
execute unless score @s cs2_team matches 1..2 run return 0
execute if score @s cs2_money matches ..2249 run tellraw @s "资金不足：需要 $2250。"
execute if score @s cs2_money matches ..2249 run return 0
scoreboard players remove @s cs2_money 2250
scoreboard players set @s cs2_wclass 4
tag @s add cs2_shop_buying
scoreboard players set @s cs2_primary 0
function cs2:shop/clear_primary
give @s tacz:modern_kinetic_gun[minecraft:custom_data={cs2_primary_gun:1b,AttachmentEXTENDED_MAG:{components:{"minecraft:custom_data":{AttachmentId:"tacz:extended_mag_1"}},count:1,id:"tacz:attachment"},AttachmentGRIP:{components:{"minecraft:custom_data":{AttachmentId:"tacz:grip_rk1_b25u"}},count:1,id:"tacz:attachment"},AttachmentMUZZLE:{components:{"minecraft:custom_data":{AttachmentId:"tacz:muzzle_brake_mastiff_sg"}},count:1,id:"tacz:attachment"},AttachmentSCOPE:{components:{"minecraft:custom_data":{AttachmentId:"tacz:sight_exp3"}},count:1,id:"tacz:attachment"},GunCurrentAmmoCount:13,GunFireMode:"AUTO",GunId:"tacz:aa12",HasBulletInBarrel:1b}]
scoreboard players set @s cs2_primary 1
tag @s remove cs2_shop_buying
tellraw @s [{"text":"已购买 ","color":"green"},{"text":"AA12","color":"yellow"},{"text":"  -$2250  余额 $","color":"gray"},{"score":{"name":"@s","objective":"cs2_money"},"color":"gold"}]
