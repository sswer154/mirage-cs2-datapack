# P90（空尖弹）  $1500
execute unless score #state cs2_state matches 2 run return 0
execute unless score @s cs2_team matches 1..2 run return 0
execute if score @s cs2_money matches ..1499 run tellraw @s "资金不足：需要 $1500。"
execute if score @s cs2_money matches ..1499 run return 0
scoreboard players remove @s cs2_money 1500
scoreboard players set @s cs2_wclass 2
tag @s add cs2_shop_buying
scoreboard players set @s cs2_primary 0
function cs2:shop/clear_primary
give @s tacz:modern_kinetic_gun[minecraft:custom_data={cs2_primary_gun:1b,AttachmentLASER:{components:{"minecraft:custom_data":{AttachmentId:"huanmeng:ss197sr_57_laser"}},count:1,id:"tacz:attachment"},GunCurrentAmmoCount:50,GunFireMode:"AUTO",GunId:"tacz:p90",HasBulletInBarrel:1b}]
scoreboard players set @s cs2_primary 1
tag @s remove cs2_shop_buying
tellraw @s [{"text":"已购买 ","color":"green"},{"text":"P90（空尖弹）","color":"yellow"},{"text":"  -$1500  余额 $","color":"gray"},{"score":{"name":"@s","objective":"cs2_money"},"color":"gold"}]
