# 检查指定地图是否完整：/function cs2:map/check {map:"dust"}
# 先给当前已加载的地图点补 forceload，避免旧地图点区块卸载后误报缺失。
function cs2:map/forceload_all
$execute if data storage cs2:map points.$(map) run function cs2:map/forceload_target {map:"$(map)"}
$tellraw @s {"text":"检查地图 $(map)：","color":"gold"}
$execute unless entity @e[type=marker,tag=cs2_map_$(map)_spawn_t,limit=1] run tellraw @s {"text":"T 出生点：缺失","color":"red"}
$execute if entity @e[type=marker,tag=cs2_map_$(map)_spawn_t,limit=1] run tellraw @s {"text":"T 出生点：已设置","color":"green"}
$execute unless entity @e[type=marker,tag=cs2_map_$(map)_spawn_ct,limit=1] run tellraw @s {"text":"CT 出生点：缺失","color":"red"}
$execute if entity @e[type=marker,tag=cs2_map_$(map)_spawn_ct,limit=1] run tellraw @s {"text":"CT 出生点：已设置","color":"green"}
$execute unless entity @e[type=marker,tag=cs2_map_$(map)_site_a,limit=1] run tellraw @s {"text":"A 包点：缺失","color":"red"}
$execute if entity @e[type=marker,tag=cs2_map_$(map)_site_a,limit=1] run tellraw @s {"text":"A 包点：已设置","color":"green"}
$execute unless entity @e[type=marker,tag=cs2_map_$(map)_site_b,limit=1] run tellraw @s {"text":"B 包点：缺失","color":"red"}
$execute if entity @e[type=marker,tag=cs2_map_$(map)_site_b,limit=1] run tellraw @s {"text":"B 包点：已设置","color":"green"}
$execute unless entity @e[type=marker,tag=cs2_map_$(map)_lobby,limit=1] run tellraw @s {"text":"大厅：缺失","color":"red"}
$execute if entity @e[type=marker,tag=cs2_map_$(map)_lobby,limit=1] run tellraw @s {"text":"大厅：已设置","color":"green"}
$execute unless entity @e[type=text_display,tag=cs2_map_$(map)_site_a_text,limit=1] run tellraw @s {"text":"A 包点文字：缺失（不影响比赛）","color":"yellow"}
$execute unless entity @e[type=text_display,tag=cs2_map_$(map)_site_b_text,limit=1] run tellraw @s {"text":"B 包点文字：缺失（不影响比赛）","color":"yellow"}
