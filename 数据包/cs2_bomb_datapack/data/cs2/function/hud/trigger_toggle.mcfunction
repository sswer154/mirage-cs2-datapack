# /trigger cs2_hud_toggle 的处理入口
scoreboard players reset @s cs2_hud_toggle
function cs2:hud/toggle
scoreboard players enable @s cs2_hud_toggle
