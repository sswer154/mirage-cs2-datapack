scoreboard players reset @s cs2_leave
scoreboard players enable @s cs2_leave
execute if score @s cs2_carrier matches 1 run function cs2:bomb/carrier_lost
clear @s minecraft:nautilus_shell[minecraft:custom_data={cs2_bomb:1b}]
function cs2:join/to_lobby
tellraw @s {"text":"已离开队伍。","color":"gray"}
