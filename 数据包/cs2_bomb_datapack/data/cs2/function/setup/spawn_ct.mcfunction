# 在玩家脚下设置 CT 出生点，并记录玩家当前朝向。
kill @e[type=marker,tag=cs2_spawn_ct]
execute at @s run summon marker ~ ~ ~ {Tags:["cs2_spawn_ct"]}
execute at @s run tp @e[type=marker,tag=cs2_spawn_ct,limit=1] ~ ~ ~ ~ ~
# 强制加载出生点区块，避免远距离配置点因区块卸载而查不到 marker
execute at @s run forceload add ~ ~
tellraw @s {"text":"CT 出生点已设置。","color":"green"}
