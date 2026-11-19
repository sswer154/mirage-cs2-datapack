# 在玩家脚下设置 T 出生点，并记录玩家当前朝向。
kill @e[type=marker,tag=cs2_spawn_t]
execute at @s run summon marker ~ ~ ~ {Tags:["cs2_spawn_t"]}
execute at @s run tp @e[type=marker,tag=cs2_spawn_t,limit=1] ~ ~ ~ ~ ~
# 强制加载出生点区块，避免远距离配置点因区块卸载而查不到 marker
execute at @s run cs2chunks retain ~ ~
tellraw @s {"text":"T 出生点已设置。","color":"green"}
