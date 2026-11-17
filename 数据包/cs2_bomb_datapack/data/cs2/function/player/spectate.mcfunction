# 阵亡后旁观本队仍存活的队友；没有存活队友时回大厅
gamemode spectator @s

execute if score @s cs2_team matches 1 if entity @a[team=cs2_t,scores={cs2_alive=1}] run spectate @r[team=cs2_t,scores={cs2_alive=1}] @s
execute if score @s cs2_team matches 2 if entity @a[team=cs2_ct,scores={cs2_alive=1}] run spectate @r[team=cs2_ct,scores={cs2_alive=1}] @s

execute if score @s cs2_team matches 1 unless entity @a[team=cs2_t,scores={cs2_alive=1}] run tp @s @e[type=marker,tag=cs2_lobby,limit=1]
execute if score @s cs2_team matches 2 unless entity @a[team=cs2_ct,scores={cs2_alive=1}] run tp @s @e[type=marker,tag=cs2_lobby,limit=1]
