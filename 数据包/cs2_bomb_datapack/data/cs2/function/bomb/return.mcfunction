# 不在包点：把消耗掉的 C4 退回
give @s minecraft:nautilus_shell[minecraft:custom_data={cs2_bomb:1b},minecraft:food={nutrition:0,saturation:0,can_always_eat:true,eat_seconds:3.2},minecraft:item_name='{"text":"C4 炸药","color":"red","bold":true}',minecraft:lore=['{"text":"在 A/B 包点长按右键安放","color":"gray"}','{"text":"安放时间 3.2 秒","color":"dark_gray"}'],minecraft:custom_model_data=721001,minecraft:enchantment_glint_override=1b]
scoreboard players set @s cs2_carrier 1
scoreboard players set #bomb cs2_bomb_state 1
title @s actionbar {"text":"必须站在 A/B 包点内才能安放","color":"red"}
