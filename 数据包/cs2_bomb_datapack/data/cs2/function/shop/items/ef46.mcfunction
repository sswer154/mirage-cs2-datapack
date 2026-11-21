# M2火焰喷射器  $4500
execute unless score #state cs2_state matches 2 run return 0
execute unless score @s cs2_team matches 1..2 run return 0
execute unless score @s cs2_team matches 1 run tellraw @s "该武器为T 专属，你无法购买。"
execute unless score @s cs2_team matches 1 run return 0
execute store result score #shop_purchase cs2_counts run cs2shop buy_gun primary 4500 tacz:modern_kinetic_gun[minecraft:custom_data={cs2_primary_gun:1b,GunCurrentAmmoCount:240,GunFireMode:"AUTO",GunId:"bf1:m2_2",HasBulletInBarrel:1b,HeatAmount:0.0f}]
execute unless score #shop_purchase cs2_counts matches 1 run return 0
scoreboard players set @s cs2_wclass 4
scoreboard players set @s cs2_primary 1
tellraw @s [{"text":"已购买 ","color":"green"},{"text":"M2火焰喷射器","color":"yellow"},{"text":"  售价 $4500  余额 $","color":"gray"},{"score":{"name":"@s","objective":"cs2_money"},"color":"gold"}]
