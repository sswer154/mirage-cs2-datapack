# 全甲  $1000（已有半甲时补 $350）
execute unless score #state cs2_state matches 2 run return 0
execute unless score @s cs2_team matches 1..2 run return 0
execute if score @s cs2_armor matches 2 run tellraw @s "你已经有全甲了。"
execute if score @s cs2_armor matches 2 run return 0
execute if score @s cs2_armor matches 1 if score @s cs2_money matches ..349 run tellraw @s "资金不足：升级全甲需要 $350。"
execute if score @s cs2_armor matches 1 if score @s cs2_money matches ..349 run return 0
execute if score @s cs2_armor matches 0 if score @s cs2_money matches ..999 run tellraw @s "资金不足：全甲需要 $1000。"
execute if score @s cs2_armor matches 0 if score @s cs2_money matches ..999 run return 0
execute if score @s cs2_armor matches 1 run scoreboard players remove @s cs2_money 350
execute if score @s cs2_armor matches 0 run scoreboard players remove @s cs2_money 1000
item replace entity @s armor.head with minecraft:netherite_helmet[minecraft:custom_data={cs2_drop_clean:1b}]
item replace entity @s armor.chest with minecraft:netherite_chestplate[minecraft:custom_data={cs2_drop_clean:1b}]
item replace entity @s armor.legs with minecraft:netherite_leggings[minecraft:custom_data={cs2_drop_clean:1b}]
item replace entity @s armor.feet with minecraft:netherite_boots[minecraft:custom_data={cs2_drop_clean:1b}]
scoreboard players set @s cs2_armor 2
tellraw @s [{"text":"已购买 ","color":"green"},{"text":"全甲","color":"yellow"},{"text":"  余额 $","color":"gray"},{"score":{"name":"@s","objective":"cs2_money"},"color":"gold"}]
