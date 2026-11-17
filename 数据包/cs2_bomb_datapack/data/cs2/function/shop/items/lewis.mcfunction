# 刘易斯轻机枪  $4500
execute unless score #state cs2_state matches 2 run return 0
execute unless score @s cs2_team matches 1..2 run return 0
execute unless score @s cs2_team matches 1 run tellraw @s "该武器为T 专属，你无法购买。"
execute unless score @s cs2_team matches 1 run return 0
execute if score @s cs2_money matches ..4499 run tellraw @s "资金不足：需要 $4500。"
execute if score @s cs2_money matches ..4499 run return 0
scoreboard players remove @s cs2_money 4500
scoreboard players set @s cs2_wclass 6
tag @s add cs2_shop_buying
scoreboard players set @s cs2_primary 0
function cs2:shop/clear_primary
give @s tacz:modern_kinetic_gun[minecraft:custom_data={cs2_primary_gun:1b,AttachmentSCOPE:{components:{"minecraft:custom_data":{AttachmentId:"tacz:scope_hamr"}},count:1,id:"tacz:attachment"},AttachmentSTOCK:{components:{"minecraft:custom_data":{AttachmentId:"tacz:stock_hk_slim_line"}},count:1,id:"tacz:attachment"},GunCurrentAmmoCount:45,GunFireMode:"AUTO",GunId:"bf1:lewis",HasBulletInBarrel:1b}]
scoreboard players set @s cs2_primary 1
tag @s remove cs2_shop_buying
tellraw @s [{"text":"已购买 ","color":"green"},{"text":"刘易斯轻机枪","color":"yellow"},{"text":"  -$4500  余额 $","color":"gray"},{"score":{"name":"@s","objective":"cs2_money"},"color":"gold"}]
