# 3 号左轮手枪  $600
execute unless score #state cs2_state matches 2 run return 0
execute unless score @s cs2_team matches 1..2 run return 0
execute if score @s cs2_secondary matches 1 run tellraw @s "你本回合已经有副武器了。"
execute if score @s cs2_secondary matches 1 run return 0
execute if score @s cs2_money matches ..599 run tellraw @s "资金不足：需要 $600。"
execute if score @s cs2_money matches ..599 run return 0
scoreboard players remove @s cs2_money 600
scoreboard players set @s cs2_wclass 1
clear @s tacz:modern_kinetic_gun[minecraft:custom_data~{GunId:"tacz:glock_17"}]
clear @s tacz:modern_kinetic_gun[minecraft:custom_data~{GunId:"tacz:m9a4"}]
scoreboard players set @s cs2_secondary 1
give @s tacz:modern_kinetic_gun[minecraft:custom_data={cs2_secondary_gun:1b,GunCurrentAmmoCount:6,GunFireMode:"SEMI",GunId:"bf1:sw_model3",HasBulletInBarrel:1b}]
tellraw @s [{"text":"已购买 ","color":"green"},{"text":"3 号左轮手枪","color":"yellow"},{"text":"  -$600  余额 $","color":"gray"},{"score":{"name":"@s","objective":"cs2_money"},"color":"gold"}]
