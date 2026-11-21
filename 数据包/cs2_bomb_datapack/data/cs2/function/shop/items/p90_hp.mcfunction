# P90（空尖弹）  $1300
execute unless score #state cs2_state matches 2 run return 0
execute unless score @s cs2_team matches 1..2 run return 0
execute store result score #shop_purchase cs2_counts run cs2shop buy_gun primary 1300 tacz:modern_kinetic_gun[minecraft:custom_data={cs2_primary_gun:1b,AttachmentLASER:{components:{"minecraft:custom_data":{AttachmentId:"huanmeng:ss197sr_57_laser"}},count:1,id:"tacz:attachment"},GunCurrentAmmoCount:50,GunFireMode:"AUTO",GunId:"tacz:p90",HasBulletInBarrel:1b}]
execute unless score #shop_purchase cs2_counts matches 1 run return 0
scoreboard players set @s cs2_wclass 2
scoreboard players set @s cs2_primary 1
tellraw @s [{"text":"已购买 ","color":"green"},{"text":"P90（空尖弹）","color":"yellow"},{"text":"  售价 $1300  余额 $","color":"gray"},{"score":{"name":"@s","objective":"cs2_money"},"color":"gold"}]
