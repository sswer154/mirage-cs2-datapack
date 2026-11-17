# 检查五个必要 marker 是否齐全
execute if entity @e[type=marker,tag=cs2_spawn_t] if entity @e[type=marker,tag=cs2_spawn_ct] if entity @e[type=marker,tag=cs2_site_a] if entity @e[type=marker,tag=cs2_site_b] if entity @e[type=marker,tag=cs2_lobby] run tellraw @s {"text":"配置完整，可以开始比赛。","color":"green"}
execute unless entity @e[type=marker,tag=cs2_spawn_t] run tellraw @s {"text":"缺少 T 出生点。","color":"red"}
execute unless entity @e[type=marker,tag=cs2_spawn_ct] run tellraw @s {"text":"缺少 CT 出生点。","color":"red"}
execute unless entity @e[type=marker,tag=cs2_site_a] run tellraw @s {"text":"缺少 A 包点。","color":"red"}
execute unless entity @e[type=marker,tag=cs2_site_b] run tellraw @s {"text":"缺少 B 包点。","color":"red"}
execute unless entity @e[type=marker,tag=cs2_lobby] run tellraw @s {"text":"缺少大厅出生点。","color":"red"}
