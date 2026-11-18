# M1895  $2550
execute unless score #state cs2_state matches 2 run return 0
execute unless score @s cs2_team matches 1..2 run return 0
execute if score @s cs2_money matches ..2549 run tellraw @s "资金不足：需要 $2550。"
execute if score @s cs2_money matches ..2549 run return 0
scoreboard players remove @s cs2_money 2550
scoreboard players set @s cs2_wclass 7
tag @s add cs2_shop_buying
scoreboard players set @s cs2_primary 0
function cs2:shop/clear_primary
give @s tacz:modern_kinetic_gun[minecraft:custom_data={cs2_primary_gun:1b,AttachmentSCOPE:{components:{"minecraft:custom_data":{AttachmentId:"tacz:scope_standard_8x"}},count:1,id:"tacz:attachment"},GunCurrentAmmoCount:4,GunFireMode:"SEMI",GunId:"bf1:man_m95",HasBulletInBarrel:1b}]
scoreboard players set @s cs2_primary 1
tag @s remove cs2_shop_buying
tellraw @s [{"text":"已购买 ","color":"green"},{"text":"M1895","color":"yellow"},{"text":"  -$2550  余额 $","color":"gray"},{"score":{"name":"@s","objective":"cs2_money"},"color":"gold"}]
