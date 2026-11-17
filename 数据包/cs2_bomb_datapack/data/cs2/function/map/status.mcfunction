# 查看当前地图与配置
tellraw @s [{"text":"当前地图：","color":"gold"},{"nbt":"active","storage":"cs2:map"}]
tellraw @s {"text":"提示：用 /function cs2:map/switch {map:\"地图名\"} 切换。","color":"gray"}
execute if entity @e[type=marker,tag=cs2_spawn_t] run tellraw @s {"text":"T 出生点已启用","color":"green"}
execute unless entity @e[type=marker,tag=cs2_spawn_t] run tellraw @s {"text":"T 出生点未启用","color":"red"}
execute if entity @e[type=marker,tag=cs2_lobby] run tellraw @s {"text":"大厅已启用","color":"green"}
execute unless entity @e[type=marker,tag=cs2_lobby] run tellraw @s {"text":"大厅未启用","color":"red"}
