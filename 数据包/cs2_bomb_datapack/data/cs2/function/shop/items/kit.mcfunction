# 拆弹器  $400（CT 专属）
execute unless score #state cs2_state matches 2 run return 0
execute unless score @s cs2_team matches 2 run tellraw @s "拆弹器仅限 CT 购买。"
execute unless score @s cs2_team matches 2 run return 0
execute if score @s cs2_kit matches 1 run tellraw @s "你已经有拆弹器了。"
execute if score @s cs2_kit matches 1 run return 0
execute if score @s cs2_money matches ..399 run tellraw @s "资金不足：拆弹器需要 $400。"
execute if score @s cs2_money matches ..399 run return 0
scoreboard players remove @s cs2_money 400
function cs2:kit/give
tellraw @s [{"text":"已购买 ","color":"green"},{"text":"拆弹器","color":"yellow"},{"text":"  -$400  余额 $","color":"gray"},{"score":{"name":"@s","objective":"cs2_money"},"color":"gold"}]
