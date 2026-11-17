scoreboard players reset @s cs2_practice_leave
scoreboard players enable @s cs2_practice_leave
execute unless score #solo cs2_counts matches 1..2 run tellraw @s {"text":"当前不是练习模式。","color":"red"}
execute unless score #solo cs2_counts matches 1..2 run return 0
execute unless score #state cs2_state matches 1..6 run tellraw @s {"text":"当前没有进行中的练习战局。","color":"red"}
execute unless score #state cs2_state matches 1..6 run return 0
tellraw @a[scores={cs2_inmatch=1}] {"text":"练习模式：退出当前战局。","color":"yellow"}
execute if entity @e[type=marker,tag=cs2_lobby,limit=1] run tp @a[scores={cs2_inmatch=1}] @e[type=marker,tag=cs2_lobby,limit=1]
function cs2:match/stop
