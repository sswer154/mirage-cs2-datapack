# M16A4  $2800
execute unless score #state cs2_state matches 2 run return 0
execute unless score @s cs2_team matches 1..2 run return 0
execute unless score @s cs2_team matches 2 run tellraw @s "该武器为CT 专属，你无法购买。"
execute unless score @s cs2_team matches 2 run return 0
execute if score @s cs2_money matches ..2799 run tellraw @s "资金不足：需要 $2800。"
execute if score @s cs2_money matches ..2799 run return 0
scoreboard players remove @s cs2_money 2800
scoreboard players set @s cs2_wclass 3
tag @s add cs2_shop_buying
scoreboard players set @s cs2_primary 0
function cs2:shop/clear_primary
give @s tacz:modern_kinetic_gun[minecraft:custom_data={cs2_primary_gun:1b,AttachmentEXTENDED_MAG:{components:{"minecraft:custom_data":{AttachmentId:"huanmeng:m856a1_556_mag"}},count:1,id:"tacz:attachment"},GunCurrentAmmoCount:30,GunFireMode:"BURST",GunId:"tacz:m16a4",HasBulletInBarrel:1b}]
scoreboard players set @s cs2_primary 1
tag @s remove cs2_shop_buying
tellraw @s [{"text":"已购买 ","color":"green"},{"text":"M16A4","color":"yellow"},{"text":"  -$2800  余额 $","color":"gray"},{"score":{"name":"@s","objective":"cs2_money"},"color":"gold"}]
