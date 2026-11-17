# 查看当前地图配置
tellraw @s [{"text":"当前地图：","color":"gold"},{"nbt":"active","storage":"cs2:map"}]
execute if entity @e[type=marker,tag=cs2_spawn_t] run tellraw @s {"text":"T 出生点: ","color":"gray","extra":[{"nbt":"Pos","entity":"@e[type=marker,tag=cs2_spawn_t,limit=1]"}]}
execute unless entity @e[type=marker,tag=cs2_spawn_t] run tellraw @s {"text":"T 出生点: 未设置","color":"red"}
execute if entity @e[type=marker,tag=cs2_spawn_ct] run tellraw @s {"text":"CT 出生点: ","color":"gray","extra":[{"nbt":"Pos","entity":"@e[type=marker,tag=cs2_spawn_ct,limit=1]"}]}
execute unless entity @e[type=marker,tag=cs2_spawn_ct] run tellraw @s {"text":"CT 出生点: 未设置","color":"red"}
execute if entity @e[type=marker,tag=cs2_site_a] run tellraw @s {"text":"A 包点: ","color":"gray","extra":[{"nbt":"Pos","entity":"@e[type=marker,tag=cs2_site_a,limit=1]"}]}
execute unless entity @e[type=marker,tag=cs2_site_a] run tellraw @s {"text":"A 包点: 未设置","color":"red"}
execute if entity @e[type=marker,tag=cs2_site_b] run tellraw @s {"text":"B 包点: ","color":"gray","extra":[{"nbt":"Pos","entity":"@e[type=marker,tag=cs2_site_b,limit=1]"}]}
execute unless entity @e[type=marker,tag=cs2_site_b] run tellraw @s {"text":"B 包点: 未设置","color":"red"}
execute if entity @e[type=marker,tag=cs2_lobby] run tellraw @s {"text":"大厅: ","color":"gray","extra":[{"nbt":"Pos","entity":"@e[type=marker,tag=cs2_lobby,limit=1]"}]}
execute unless entity @e[type=marker,tag=cs2_lobby] run tellraw @s {"text":"大厅: 未设置","color":"red"}
