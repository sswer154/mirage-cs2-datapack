scoreboard players reset @s cs2_leave
scoreboard players enable @s cs2_leave
execute unless score @s cs2_team matches 1..3 unless score @s cs2_inmatch matches 1 unless function cs2:player/has_saved_inventory run return 0
execute if score @s cs2_carrier matches 1 run function cs2:bomb/carrier_lost
clear @s minecraft:nautilus_shell[minecraft:custom_data={cs2_bomb:1b}]
cs2roundstate discard
function cs2:music/stop
function cs2:join/to_lobby
tellraw @s {"text":"已离开队伍。","color":"gray"}
