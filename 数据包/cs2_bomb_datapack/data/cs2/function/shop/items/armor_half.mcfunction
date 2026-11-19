# 半甲  $650
execute unless score #state cs2_state matches 2 run return 0
execute unless score @s cs2_team matches 1..2 run return 0
execute unless score @s cs2_armor matches 0 run tellraw @s "你已经有护甲了。"
execute unless score @s cs2_armor matches 0 run return 0
execute if score @s cs2_money matches ..649 run tellraw @s "资金不足：半甲需要 $650。"
execute if score @s cs2_money matches ..649 run return 0
scoreboard players remove @s cs2_money 650
item replace entity @s armor.head with minecraft:netherite_helmet[minecraft:custom_data={cs2_drop_clean:1b}]
item replace entity @s armor.chest with minecraft:netherite_chestplate[minecraft:custom_data={cs2_drop_clean:1b}]
scoreboard players set @s cs2_armor 1
tellraw @s [{"text":"已购买 ","color":"green"},{"text":"半甲","color":"yellow"},{"text":"  -$650  余额 $","color":"gray"},{"score":{"name":"@s","objective":"cs2_money"},"color":"gold"}]
