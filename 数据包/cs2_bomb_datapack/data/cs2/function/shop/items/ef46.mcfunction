# M2火焰喷射器  $4800
execute unless score #state cs2_state matches 2 run return 0
execute unless score @s cs2_team matches 1..2 run return 0
execute unless score @s cs2_team matches 1 run tellraw @s "该武器为T 专属，你无法购买。"
execute unless score @s cs2_team matches 1 run return 0
execute if score @s cs2_money matches ..4799 run tellraw @s "资金不足：需要 $4800。"
execute if score @s cs2_money matches ..4799 run return 0
scoreboard players remove @s cs2_money 4800
scoreboard players set @s cs2_wclass 4
tag @s add cs2_shop_buying
scoreboard players set @s cs2_primary 0
function cs2:shop/clear_primary
give @s tacz:modern_kinetic_gun[minecraft:custom_data={cs2_primary_gun:1b,GunCurrentAmmoCount:240,GunFireMode:"AUTO",GunId:"bf1:m2_2",HasBulletInBarrel:1b,HeatAmount:0.0f}]
scoreboard players set @s cs2_primary 1
tag @s remove cs2_shop_buying
tellraw @s [{"text":"已购买 ","color":"green"},{"text":"M2火焰喷射器","color":"yellow"},{"text":"  -$4800  余额 $","color":"gray"},{"score":{"name":"@s","objective":"cs2_money"},"color":"gold"}]
