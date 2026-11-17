# 维拉佩罗萨  $2000
execute unless score #state cs2_state matches 2 run return 0
execute unless score @s cs2_team matches 1..2 run return 0
execute if score @s cs2_money matches ..1999 run tellraw @s "资金不足：需要 $2000。"
execute if score @s cs2_money matches ..1999 run return 0
scoreboard players remove @s cs2_money 2000
scoreboard players set @s cs2_wclass 2
tag @s add cs2_shop_buying
scoreboard players set @s cs2_primary 0
function cs2:shop/clear_primary
give @s tacz:modern_kinetic_gun[minecraft:custom_data={cs2_primary_gun:1b,GunCurrentAmmoCount:50,GunFireMode:"BURST",GunId:"bf1:vp1915",HasBulletInBarrel:1b}]
scoreboard players set @s cs2_primary 1
tag @s remove cs2_shop_buying
tellraw @s [{"text":"已购买 ","color":"green"},{"text":"维拉佩罗萨","color":"yellow"},{"text":"  -$2000  余额 $","color":"gray"},{"score":{"name":"@s","objective":"cs2_money"},"color":"gold"}]
