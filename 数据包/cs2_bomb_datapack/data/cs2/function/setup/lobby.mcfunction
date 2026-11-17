# 在玩家脚下设置大厅/观战出生点。
kill @e[type=marker,tag=cs2_lobby]
execute at @s run summon marker ~ ~ ~ {Tags:["cs2_lobby"]}
execute at @s run tp @e[type=marker,tag=cs2_lobby,limit=1] ~ ~ ~ ~ ~
# 强制加载大厅区块，避免远距离配置点因区块卸载而查不到 marker
execute at @s run forceload add ~ ~
tellraw @s {"text":"大厅出生点已设置。","color":"green"}
