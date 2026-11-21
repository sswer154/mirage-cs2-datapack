# MP5A5  $1050
execute unless score #state cs2_state matches 2 run return 0
execute unless score @s cs2_team matches 1..2 run return 0
execute unless score @s cs2_team matches 2 run tellraw @s "该武器为CT 专属，你无法购买。"
execute unless score @s cs2_team matches 2 run return 0
execute store result score #shop_purchase cs2_counts run cs2shop buy_gun primary 1050 tacz:modern_kinetic_gun[minecraft:custom_data={cs2_primary_gun:1b,AttachmentEXTENDED_MAG:{components:{"minecraft:custom_data":{AttachmentId:"huanmeng:n7n31_9x19_mag"}},count:1,id:"tacz:attachment"},GunCurrentAmmoCount:60,GunFireMode:"AUTO",GunId:"tacz:hk_mp5a5",HasBulletInBarrel:1b}]
execute unless score #shop_purchase cs2_counts matches 1 run return 0
scoreboard players set @s cs2_wclass 2
scoreboard players set @s cs2_primary 1
tellraw @s [{"text":"已购买 ","color":"green"},{"text":"MP5A5","color":"yellow"},{"text":"  售价 $1050  余额 $","color":"gray"},{"score":{"name":"@s","objective":"cs2_money"},"color":"gold"}]
