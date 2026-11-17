# 金星万金油  $850
execute unless score #state cs2_state matches 2 run return 0
execute unless score @s cs2_team matches 1..2 run return 0
function cs2:shop/count_util
execute if score #util_golden cs2_counts matches 1.. run tellraw @s "你身上已经有金星万金油了。"
execute if score #util_golden cs2_counts matches 1.. run return 0
scoreboard players operation #util_used cs2_counts = @s cs2_carry_start
scoreboard players operation #util_used cs2_counts += @s cs2_item_count
execute if score #util_used cs2_counts matches 5.. run tellraw @s "本回合道具已达上限（带入 + 已购共 5 份）。"
execute if score #util_used cs2_counts matches 5.. run return 0
execute if score @s cs2_money matches ..849 run tellraw @s "资金不足：金星万金油需要 $850。"
execute if score @s cs2_money matches ..849 run return 0
scoreboard players remove @s cs2_money 850
function cs2:util/give_golden_star
tellraw @s [{"text":"已购买 ","color":"green"},{"text":"金星万金油","color":"yellow"},{"text":"  -$850  余额 $","color":"gray"},{"score":{"name":"@s","objective":"cs2_money"},"color":"gold"}]
scoreboard players set @s cs2_util_golden 1
scoreboard players add @s cs2_item_count 1
