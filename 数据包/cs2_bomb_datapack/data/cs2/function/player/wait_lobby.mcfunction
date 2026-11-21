# 中场/换边期间重连：保留队伍，先去大厅
scoreboard players set @s cs2_alive 0
# 中场换边会清除存活标记，按当前模式保留已经在观战的玩家。
gamemode adventure @s[gamemode=!spectator]
execute if entity @e[type=marker,tag=cs2_lobby] run tp @s @e[type=marker,tag=cs2_lobby,limit=1]
tellraw @s {"text":"换边阶段，请等待下一回合。","color":"gray"}
