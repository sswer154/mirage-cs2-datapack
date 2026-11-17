execute if score @s cs2_panel matches 99 run tellraw @s {"text":"战术菜单已关闭。","color":"gray"}
execute unless score @s cs2_panel matches 99 run function cs2:panel/open
scoreboard players reset @s cs2_panel
scoreboard players enable @s cs2_panel
