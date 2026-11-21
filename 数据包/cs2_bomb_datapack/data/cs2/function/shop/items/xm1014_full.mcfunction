# 满改 M1014  $3750
execute unless score #state cs2_state matches 2 run return 0
execute unless score @s cs2_team matches 1..2 run return 0
execute store result score #shop_purchase cs2_counts run cs2shop buy_gun primary 3750 tacz:modern_kinetic_gun[minecraft:custom_data={cs2_primary_gun:1b,AttachmentEXTENDED_MAG:{components:{"minecraft:custom_data":{AttachmentId:"tacz:ammo_mod_fmj"}},count:1,id:"tacz:attachment"},AttachmentMUZZLE:{components:{"minecraft:custom_data":{AttachmentId:"tacz:muzzle_brake_mastiff_sg"}},count:1,id:"tacz:attachment"},AttachmentSCOPE:{components:{"minecraft:custom_data":{AttachmentId:"tacz:sight_coyote"}},count:1,id:"tacz:attachment"},GunCurrentAmmoCount:12,GunFireMode:"SEMI",GunId:"tacz:m1014",HasBulletInBarrel:1b}]
execute unless score #shop_purchase cs2_counts matches 1 run return 0
scoreboard players set @s cs2_wclass 4
scoreboard players set @s cs2_primary 1
tellraw @s [{"text":"已购买 ","color":"green"},{"text":"满改 M1014","color":"yellow"},{"text":"  售价 $3750  余额 $","color":"gray"},{"score":{"name":"@s","objective":"cs2_money"},"color":"gold"}]
