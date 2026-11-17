# 中场/换边期间重连：保留队伍，先去大厅
scoreboard players set @s cs2_alive 0
gamemode adventure @s
execute if entity @e[type=marker,tag=cs2_lobby] run tp @s @e[type=marker,tag=cs2_lobby,limit=1]
tellraw @s {"text":"换边阶段，请等待下一回合。","color":"gray"}
