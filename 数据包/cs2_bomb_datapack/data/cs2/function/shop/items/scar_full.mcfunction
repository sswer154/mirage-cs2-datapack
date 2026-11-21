# 满改 SCAR-L  $3200
execute unless score #state cs2_state matches 2 run return 0
execute unless score @s cs2_team matches 1..2 run return 0
execute unless score @s cs2_team matches 1 run tellraw @s "该武器为T 专属，你无法购买。"
execute unless score @s cs2_team matches 1 run return 0
execute store result score #shop_purchase cs2_counts run cs2shop buy_gun primary 3200 tacz:modern_kinetic_gun[minecraft:custom_data={cs2_primary_gun:1b,AttachmentGRIP:{components:{"minecraft:custom_data":{AttachmentId:"tacz:grip_se_5"}},count:1,id:"tacz:attachment"},AttachmentMUZZLE:{components:{"minecraft:custom_data":{AttachmentId:"tacz:muzzle_brake_cthulhu"}},count:1,id:"tacz:attachment"},AttachmentSCOPE:{components:{"minecraft:custom_data":{AttachmentId:"tacz:scope_hamr"}},count:1,id:"tacz:attachment"},AttachmentSTOCK:{components:{"minecraft:custom_data":{AttachmentId:"tacz:stock_tactical_ar"}},count:1,id:"tacz:attachment"},GunCurrentAmmoCount:30,GunFireMode:"AUTO",GunId:"tacz:scar_l",HasBulletInBarrel:1b}]
execute unless score #shop_purchase cs2_counts matches 1 run return 0
scoreboard players set @s cs2_wclass 3
scoreboard players set @s cs2_primary 1
tellraw @s [{"text":"已购买 ","color":"green"},{"text":"满改 SCAR-L","color":"yellow"},{"text":"  售价 $3200  余额 $","color":"gray"},{"score":{"name":"@s","objective":"cs2_money"},"color":"gold"}]
