# 燃烧弹  $400
execute unless score #state cs2_state matches 2 run return 0
execute unless score @s cs2_team matches 1..2 run return 0
function cs2:shop/count_util
execute if score #util_fire cs2_counts matches 1.. run tellraw @s "你身上已经有燃烧弹了。"
execute if score #util_fire cs2_counts matches 1.. run return 0
scoreboard players operation #util_used cs2_counts = @s cs2_carry_start
scoreboard players operation #util_used cs2_counts += @s cs2_item_count
execute if score #util_used cs2_counts matches 5.. run tellraw @s "本回合道具已达上限（带入 + 已购共 5 份）。"
execute if score #util_used cs2_counts matches 5.. run return 0
execute if score @s cs2_money matches ..399 run tellraw @s "资金不足：燃烧弹需要 $400。"
execute if score @s cs2_money matches ..399 run return 0
scoreboard players remove @s cs2_money 400
function cs2:util/give_fire
tellraw @s [{"text":"已购买 ","color":"green"},{"text":"燃烧弹","color":"yellow"},{"text":"  -$400  余额 $","color":"gray"},{"score":{"name":"@s","objective":"cs2_money"},"color":"gold"}]
scoreboard players set @s cs2_util_fire 1
scoreboard players add @s cs2_item_count 1
