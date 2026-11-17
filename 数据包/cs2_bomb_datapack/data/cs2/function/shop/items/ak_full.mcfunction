# 满改 AKM  $2700
execute unless score #state cs2_state matches 2 run return 0
execute unless score @s cs2_team matches 1..2 run return 0
execute unless score @s cs2_team matches 1 run tellraw @s "该武器为T 专属，你无法购买。"
execute unless score @s cs2_team matches 1 run return 0
execute if score @s cs2_money matches ..2699 run tellraw @s "资金不足：需要 $2700。"
execute if score @s cs2_money matches ..2699 run return 0
scoreboard players remove @s cs2_money 2700
scoreboard players set @s cs2_wclass 3
tag @s add cs2_shop_buying
scoreboard players set @s cs2_primary 0
function cs2:shop/clear_primary
give @s tacz:modern_kinetic_gun[minecraft:custom_data={cs2_primary_gun:1b,AttachmentEXTENDED_MAG:{components:{"minecraft:custom_data":{AttachmentId:"huanmeng:ps_762x39_mag"}},count:1,id:"tacz:attachment"},AttachmentMUZZLE:{components:{"minecraft:custom_data":{AttachmentId:"tacz:muzzle_brake_cyclone_d2"}},count:1,id:"tacz:attachment"},AttachmentSCOPE:{components:{"minecraft:custom_data":{AttachmentId:"tacz:scope_hamr"}},count:1,id:"tacz:attachment"},AttachmentSTOCK:{components:{"minecraft:custom_data":{AttachmentId:"tacz:oem_stock_tactical"}},count:1,id:"tacz:attachment"},GunCurrentAmmoCount:25,GunFireMode:"AUTO",GunId:"tacz:ak47",HasBulletInBarrel:1b}]
scoreboard players set @s cs2_primary 1
tag @s remove cs2_shop_buying
tellraw @s [{"text":"已购买 ","color":"green"},{"text":"满改 AKM","color":"yellow"},{"text":"  -$2700  余额 $","color":"gray"},{"score":{"name":"@s","objective":"cs2_money"},"color":"gold"}]
