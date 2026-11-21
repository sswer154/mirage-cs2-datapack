# 3 号左轮手枪  $600
execute unless score #state cs2_state matches 2 run return 0
execute unless score @s cs2_team matches 1..2 run return 0
execute store result score #shop_purchase cs2_counts run cs2shop buy_gun secondary 600 tacz:modern_kinetic_gun[minecraft:custom_data={cs2_secondary_gun:1b,GunCurrentAmmoCount:6,GunFireMode:"SEMI",GunId:"bf1:sw_model3",HasBulletInBarrel:1b}]
execute unless score #shop_purchase cs2_counts matches 1 run return 0
scoreboard players set @s cs2_wclass 1
scoreboard players set @s cs2_secondary 1
tellraw @s [{"text":"已购买 ","color":"green"},{"text":"3 号左轮手枪","color":"yellow"},{"text":"  售价 $600  余额 $","color":"gray"},{"score":{"name":"@s","objective":"cs2_money"},"color":"gold"}]
