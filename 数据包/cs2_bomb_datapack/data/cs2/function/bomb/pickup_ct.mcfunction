# CT 不能携带 C4：丢掉一个，并给 20 tick 拾取延迟
execute unless score #state cs2_state matches 3 run return 0
scoreboard players set @s cs2_carrier 0
clear @s minecraft:nautilus_shell[minecraft:custom_data={cs2_bomb:1b}] 1
execute at @s run summon item ~ ~ ~ {Item:{id:"minecraft:nautilus_shell",count:1,components:{"minecraft:custom_data":{cs2_bomb:1b},"minecraft:food":{nutrition:0,saturation:0,can_always_eat:true,eat_seconds:3.2},"minecraft:item_name":'{"text":"C4 炸药","color":"red","bold":true}',"minecraft:lore":['{"text":"在 A/B 包点长按右键安放","color":"gray"}','{"text":"安放时间 3.2 秒","color":"dark_gray"}'],"minecraft:custom_model_data":721001,"minecraft:enchantment_glint_override":1b}},PickupDelay:20s}
scoreboard players set #bomb cs2_bomb_state 2
scoreboard players set #drop_timer cs2_counts 100
title @s actionbar {"text":"CT 不能携带 C4，已丢下","color":"yellow"}
function cs2:bomb/normalize_c4
