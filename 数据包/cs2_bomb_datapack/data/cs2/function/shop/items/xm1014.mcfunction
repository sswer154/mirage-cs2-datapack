# M1014  $2950
execute unless score #state cs2_state matches 2 run return 0
execute unless score @s cs2_team matches 1..2 run return 0
execute if score @s cs2_money matches ..2949 run tellraw @s "资金不足：需要 $2950。"
execute if score @s cs2_money matches ..2949 run return 0
scoreboard players remove @s cs2_money 2950
scoreboard players set @s cs2_wclass 4
tag @s add cs2_shop_buying
scoreboard players set @s cs2_primary 0
function cs2:shop/clear_primary
give @s tacz:modern_kinetic_gun[minecraft:custom_data={cs2_primary_gun:1b,GunCurrentAmmoCount:6,GunFireMode:"SEMI",GunId:"tacz:m1014",HasBulletInBarrel:1b}]
scoreboard players set @s cs2_primary 1
tag @s remove cs2_shop_buying
tellraw @s [{"text":"已购买 ","color":"green"},{"text":"M1014","color":"yellow"},{"text":"  -$2950  余额 $","color":"gray"},{"score":{"name":"@s","objective":"cs2_money"},"color":"gold"}]
