# 购买前先核对副武器分数：枪已经丢了就不该拦购买
function cs2:shop/reconcile_secondary
# CZ75  $300
execute unless score #state cs2_state matches 2 run return 0
execute unless score @s cs2_team matches 1..2 run return 0
execute if score @s cs2_secondary matches 1 run tellraw @s "你本回合已经有副武器了。"
execute if score @s cs2_secondary matches 1 run return 0
execute if score @s cs2_money matches ..299 run tellraw @s "资金不足：需要 $300。"
execute if score @s cs2_money matches ..299 run return 0
scoreboard players remove @s cs2_money 300
scoreboard players set @s cs2_wclass 1
clear @s tacz:modern_kinetic_gun[minecraft:custom_data~{GunId:"tacz:glock_17"}]
clear @s tacz:modern_kinetic_gun[minecraft:custom_data~{GunId:"tacz:m9a4"}]
scoreboard players set @s cs2_secondary 1
give @s tacz:modern_kinetic_gun[minecraft:custom_data={cs2_secondary_gun:1b,AttachmentEXTENDED_MAG:{components:{"minecraft:custom_data":{AttachmentId:"huanmeng:n7n31_9x19_mag"}},count:1,id:"tacz:attachment"},GunCurrentAmmoCount:27,GunFireMode:"AUTO",GunId:"tacz:cz75",HasBulletInBarrel:1b}]
tellraw @s [{"text":"已购买 ","color":"green"},{"text":"CZ75","color":"yellow"},{"text":"  -$300  余额 $","color":"gray"},{"score":{"name":"@s","objective":"cs2_money"},"color":"gold"}]
