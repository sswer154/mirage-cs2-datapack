# AA12  $2300
execute unless score #state cs2_state matches 2 run return 0
execute unless score @s cs2_team matches 1..2 run return 0
execute store result score #shop_purchase cs2_counts run cs2shop buy_gun primary 2300 tacz:modern_kinetic_gun[minecraft:custom_data={cs2_primary_gun:1b,AttachmentEXTENDED_MAG:{components:{"minecraft:custom_data":{AttachmentId:"tacz:extended_mag_1"}},count:1,id:"tacz:attachment"},AttachmentGRIP:{components:{"minecraft:custom_data":{AttachmentId:"tacz:grip_rk1_b25u"}},count:1,id:"tacz:attachment"},AttachmentMUZZLE:{components:{"minecraft:custom_data":{AttachmentId:"tacz:muzzle_brake_mastiff_sg"}},count:1,id:"tacz:attachment"},AttachmentSCOPE:{components:{"minecraft:custom_data":{AttachmentId:"tacz:sight_exp3"}},count:1,id:"tacz:attachment"},GunCurrentAmmoCount:16,GunFireMode:"AUTO",GunId:"tacz:aa12",HasBulletInBarrel:1b}]
execute unless score #shop_purchase cs2_counts matches 1 run return 0
scoreboard players set @s cs2_wclass 4
scoreboard players set @s cs2_primary 1
tellraw @s [{"text":"已购买 ","color":"green"},{"text":"AA12","color":"yellow"},{"text":"  售价 $2300  余额 $","color":"gray"},{"score":{"name":"@s","objective":"cs2_money"},"color":"gold"}]
