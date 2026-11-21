# 发 C4 前先清掉旧的，保证身上只有一把
clear @s minecraft:nautilus_shell[minecraft:custom_data={cs2_bomb:1b}]
cs2c4 give minecraft:nautilus_shell[minecraft:custom_data={cs2_bomb:1b},minecraft:item_name='{"text":"C4 炸药","color":"red","bold":true}',minecraft:lore=['{"text":"在 A/B 包点右键一次开始安放","color":"gray"}','{"text":"默认安放时间 3.2 秒","color":"dark_gray"}'],minecraft:custom_model_data=721001,minecraft:enchantment_glint_override=1b]
execute if score @s cs2_carrier matches 1 run title @s actionbar {"text":"你拿到了 C4，去 A/B 包点安放","color":"red"}
execute if score @s cs2_carrier matches 1 run playsound minecraft:entity.item.pickup master @s
