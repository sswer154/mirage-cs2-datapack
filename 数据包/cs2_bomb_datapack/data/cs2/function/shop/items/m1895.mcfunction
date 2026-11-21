# M1895  $2550
execute unless score #state cs2_state matches 2 run return 0
execute unless score @s cs2_team matches 1..2 run return 0
execute store result score #shop_purchase cs2_counts run cs2shop buy_gun primary 2550 tacz:modern_kinetic_gun[minecraft:custom_data={cs2_primary_gun:1b,AttachmentSCOPE:{components:{"minecraft:custom_data":{AttachmentId:"tacz:scope_standard_8x"}},count:1,id:"tacz:attachment"},GunCurrentAmmoCount:4,GunFireMode:"SEMI",GunId:"bf1:man_m95",HasBulletInBarrel:1b}]
execute unless score #shop_purchase cs2_counts matches 1 run return 0
scoreboard players set @s cs2_wclass 7
scoreboard players set @s cs2_primary 1
tellraw @s [{"text":"已购买 ","color":"green"},{"text":"M1895","color":"yellow"},{"text":"  售价 $2550  余额 $","color":"gray"},{"score":{"name":"@s","objective":"cs2_money"},"color":"gold"}]
