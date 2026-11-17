# 碰炸手雷  $600
execute unless score #state cs2_state matches 2 run return 0
execute unless score @s cs2_team matches 1..2 run return 0
function cs2:shop/count_util
execute if score #util_impact cs2_counts matches 1.. run tellraw @s "你身上已经有碰炸手雷了。"
execute if score #util_impact cs2_counts matches 1.. run return 0
scoreboard players operation #util_used cs2_counts = @s cs2_carry_start
scoreboard players operation #util_used cs2_counts += @s cs2_item_count
execute if score #util_used cs2_counts matches 5.. run tellraw @s "本回合道具已达上限（带入 + 已购共 5 份）。"
execute if score #util_used cs2_counts matches 5.. run return 0
execute if score @s cs2_money matches ..599 run tellraw @s "资金不足：碰炸手雷需要 $600。"
execute if score @s cs2_money matches ..599 run return 0
scoreboard players remove @s cs2_money 600
function cs2:util/give_he_impact_proto
tellraw @s [{"text":"已购买 ","color":"green"},{"text":"碰炸手雷","color":"yellow"},{"text":"  -$600  余额 $","color":"gray"},{"score":{"name":"@s","objective":"cs2_money"},"color":"gold"}]
scoreboard players set @s cs2_util_impact 1
scoreboard players add @s cs2_item_count 1
