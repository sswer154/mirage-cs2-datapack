execute unless function cs2:player/save_inventory run return fail
execute if entity @s[team=cs2_t] run team leave @s
execute if entity @s[team=cs2_ct] run team leave @s
execute if entity @s[team=cs2_cast] run team leave @s
scoreboard players set @s cs2_team 0
scoreboard players set @s cs2_inmatch 0
scoreboard players reset @s cs2_ready_team
function cs2:player/tag
clear @s
effect clear @s
function cs2:player/make_spec
execute if entity @e[type=marker,tag=cs2_lobby,limit=1] run tp @s @e[type=marker,tag=cs2_lobby,limit=1]
return 1
