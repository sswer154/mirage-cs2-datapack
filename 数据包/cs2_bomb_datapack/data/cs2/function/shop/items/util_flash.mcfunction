# 闪光弹  $200
execute unless score #state cs2_state matches 2 run return 0
execute unless score @s cs2_team matches 1..2 run return 0
function cs2:shop/count_util
execute if score #util_flash cs2_counts matches 2.. run tellraw @s "最多携带 2 颗闪光弹。"
execute if score #util_flash cs2_counts matches 2.. run return 0
scoreboard players operation #util_used cs2_counts = @s cs2_carry_start
scoreboard players operation #util_used cs2_counts += @s cs2_item_count
execute if score #util_used cs2_counts matches 5.. run tellraw @s "本回合道具已达上限（带入 + 已购共 5 份）。"
execute if score #util_used cs2_counts matches 5.. run return 0
execute if score @s cs2_money matches ..199 run tellraw @s "资金不足：闪光弹需要 $200。"
execute if score @s cs2_money matches ..199 run return 0
scoreboard players remove @s cs2_money 200
function cs2:util/give_flash
tellraw @s [{"text":"已购买 ","color":"green"},{"text":"闪光弹","color":"yellow"},{"text":"  -$200  余额 $","color":"gray"},{"score":{"name":"@s","objective":"cs2_money"},"color":"gold"}]
execute store result score @s cs2_util_flash run clear @s minecraft:snowball[minecraft:custom_data~{cs2_util:"flash"}] 0
scoreboard players add @s cs2_item_count 1
