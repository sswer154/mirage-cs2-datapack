# 春田 1873  $2050
execute unless score #state cs2_state matches 2 run return 0
execute unless score @s cs2_team matches 1..2 run return 0
execute if score @s cs2_money matches ..2049 run tellraw @s "资金不足：需要 $2050。"
execute if score @s cs2_money matches ..2049 run return 0
scoreboard players remove @s cs2_money 2050
scoreboard players set @s cs2_wclass 7
tag @s add cs2_shop_buying
scoreboard players set @s cs2_primary 0
function cs2:shop/clear_primary
give @s tacz:modern_kinetic_gun[minecraft:custom_data={cs2_primary_gun:1b,AttachmentSCOPE:{components:{"cmagic:item_volume":0.2f,"minecraft:custom_data":{AttachmentId:"tacz:scope_1873_6x"}},count:1,id:"tacz:attachment"},GunCurrentAmmoCount:1,GunFireMode:"SEMI",GunId:"tacz:springfield1873",HasBulletInBarrel:1b}]
scoreboard players set @s cs2_primary 1
tag @s remove cs2_shop_buying
tellraw @s [{"text":"已购买 ","color":"green"},{"text":"春田 1873","color":"yellow"},{"text":"  -$2050  余额 $","color":"gray"},{"score":{"name":"@s","objective":"cs2_money"},"color":"gold"}]
