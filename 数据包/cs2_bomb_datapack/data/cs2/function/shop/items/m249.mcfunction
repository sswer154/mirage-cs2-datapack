# M249  $5000
execute unless score #state cs2_state matches 2 run return 0
execute unless score @s cs2_team matches 1..2 run return 0
execute unless score @s cs2_team matches 2 run tellraw @s "该武器为CT 专属，你无法购买。"
execute unless score @s cs2_team matches 2 run return 0
execute if score @s cs2_money matches ..4999 run tellraw @s "资金不足：需要 $5000。"
execute if score @s cs2_money matches ..4999 run return 0
scoreboard players remove @s cs2_money 5000
scoreboard players set @s cs2_wclass 6
tag @s add cs2_shop_buying
scoreboard players set @s cs2_primary 0
function cs2:shop/clear_primary
give @s tacz:modern_kinetic_gun[minecraft:custom_data={cs2_primary_gun:1b,AttachmentGRIP:{components:{"minecraft:custom_data":{AttachmentId:"tacz:grip_vertical_talon"}},count:1,id:"tacz:attachment"},AttachmentMUZZLE:{components:{"minecraft:custom_data":{AttachmentId:"tacz:muzzle_brake_cyclone_d2"}},count:1,id:"tacz:attachment"},AttachmentSCOPE:{components:{"minecraft:custom_data":{AttachmentId:"tacz:scope_hamr"}},count:1,id:"tacz:attachment"},GunCurrentAmmoCount:48,GunFireMode:"AUTO",GunId:"tacz:m249",HasBulletInBarrel:1b}]
scoreboard players set @s cs2_primary 1
tag @s remove cs2_shop_buying
tellraw @s [{"text":"已购买 ","color":"green"},{"text":"M249","color":"yellow"},{"text":"  -$5000  余额 $","color":"gray"},{"score":{"name":"@s","objective":"cs2_money"},"color":"gold"}]
