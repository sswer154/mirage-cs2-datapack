gamemode adventure @s
clear @s
effect clear @s
function cs2:util/speed
function cs2:util/jump_unlock
scoreboard players set @s cs2_alive 0
scoreboard players set @s cs2_was_alive 0
scoreboard players set @s cs2_needs_default 1
execute if entity @e[type=marker,tag=cs2_lobby] run tp @s @e[type=marker,tag=cs2_lobby,limit=1]
