scoreboard players set #blink cs2_blink 0
scoreboard players operation #blink_old cs2_blink = #blink_on cs2_blink
execute if score #blink_old cs2_blink matches 0 run data modify entity @e[type=item_display,tag=cs2_c4_display,limit=1] item.components."minecraft:custom_model_data" set value 721002
execute if score #blink_old cs2_blink matches 0 run scoreboard players set #blink_on cs2_blink 1
execute if score #blink_old cs2_blink matches 1 run data modify entity @e[type=item_display,tag=cs2_c4_display,limit=1] item.components."minecraft:custom_model_data" set value 721001
execute if score #blink_old cs2_blink matches 1 run scoreboard players set #blink_on cs2_blink 0
