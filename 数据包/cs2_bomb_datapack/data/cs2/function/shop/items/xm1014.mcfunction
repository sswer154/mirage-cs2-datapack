# M1014  $2800
execute unless score #state cs2_state matches 2 run return 0
execute unless score @s cs2_team matches 1..2 run return 0
execute store result score #shop_purchase cs2_counts run cs2shop buy_gun primary 2800 tacz:modern_kinetic_gun[minecraft:custom_data={cs2_primary_gun:1b,GunCurrentAmmoCount:6,GunFireMode:"SEMI",GunId:"tacz:m1014",HasBulletInBarrel:1b}]
execute unless score #shop_purchase cs2_counts matches 1 run return 0
scoreboard players set @s cs2_wclass 4
scoreboard players set @s cs2_primary 1
tellraw @s [{"text":"已购买 ","color":"green"},{"text":"M1014","color":"yellow"},{"text":"  售价 $2800  余额 $","color":"gray"},{"score":{"name":"@s","objective":"cs2_money"},"color":"gold"}]
