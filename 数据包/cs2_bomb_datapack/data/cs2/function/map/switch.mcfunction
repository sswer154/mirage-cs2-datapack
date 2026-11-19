# 一键切换到指定地图：/function cs2:map/switch {map:"dust"}
# 只能在空闲状态（state 0）切换，避免比赛中把包点/出生点换掉。
execute unless score #state cs2_state matches 0 run tellraw @s {"text":"请先结束/停止比赛再切换地图。","color":"red"}
execute unless score #state cs2_state matches 0 run return 0

# 先给当前已加载的地图点补 forceload，避免旧地图点区块卸载后检查不到 marker。
function cs2:map/forceload_all
$execute if data storage cs2:map points.$(map) run function cs2:map/forceload_target {map:"$(map)"}

# 检查地图是否完整
$execute unless entity @e[type=marker,tag=cs2_map_$(map)_spawn_t,limit=1] run tellraw @s {"text":"该地图缺少 T 出生点。","color":"red"}
$execute unless entity @e[type=marker,tag=cs2_map_$(map)_spawn_t,limit=1] run return 0
$execute unless entity @e[type=marker,tag=cs2_map_$(map)_spawn_ct,limit=1] run tellraw @s {"text":"该地图缺少 CT 出生点。","color":"red"}
$execute unless entity @e[type=marker,tag=cs2_map_$(map)_spawn_ct,limit=1] run return 0
$execute unless entity @e[type=marker,tag=cs2_map_$(map)_site_a,limit=1] run tellraw @s {"text":"该地图缺少 A 包点。","color":"red"}
$execute unless entity @e[type=marker,tag=cs2_map_$(map)_site_a,limit=1] run return 0
$execute unless entity @e[type=marker,tag=cs2_map_$(map)_site_b,limit=1] run tellraw @s {"text":"该地图缺少 B 包点。","color":"red"}
$execute unless entity @e[type=marker,tag=cs2_map_$(map)_site_b,limit=1] run return 0
$execute unless entity @e[type=marker,tag=cs2_map_$(map)_lobby,limit=1] run tellraw @s {"text":"该地图缺少大厅出生点。","color":"red"}
$execute unless entity @e[type=marker,tag=cs2_map_$(map)_lobby,limit=1] run return 0

# 先移除所有地图的“当前启用”标签
tag @e[type=marker] remove cs2_spawn_t
tag @e[type=marker] remove cs2_spawn_ct
tag @e[type=marker] remove cs2_site_a
tag @e[type=marker] remove cs2_site_b
tag @e[type=marker] remove cs2_lobby
tag @e[type=text_display] remove cs2_site_a_text
tag @e[type=text_display] remove cs2_site_b_text
# 所有地图的包点文字先隐藏
execute as @e[type=text_display,tag=cs2_map_text] run data merge entity @s {text:"{\"text\":\"\"}"}

# 给选中地图的实体加“当前启用”标签
$tag @e[type=marker,tag=cs2_map_$(map)_spawn_t] add cs2_spawn_t
$tag @e[type=marker,tag=cs2_map_$(map)_spawn_ct] add cs2_spawn_ct
$tag @e[type=marker,tag=cs2_map_$(map)_site_a] add cs2_site_a
$tag @e[type=marker,tag=cs2_map_$(map)_site_b] add cs2_site_b
$tag @e[type=marker,tag=cs2_map_$(map)_lobby] add cs2_lobby
$tag @e[type=text_display,tag=cs2_map_$(map)_site_a_text] add cs2_site_a_text
$tag @e[type=text_display,tag=cs2_map_$(map)_site_b_text] add cs2_site_b_text

# 只显示选中地图的包点文字
$execute as @e[type=text_display,tag=cs2_map_$(map)_site_a_text,limit=1] run data merge entity @s {text:"{\"text\":\"A 包点\",\"color\":\"red\",\"bold\":true}"}
$execute as @e[type=text_display,tag=cs2_map_$(map)_site_b_text,limit=1] run data merge entity @s {text:"{\"text\":\"B 包点\",\"color\":\"red\",\"bold\":true}"}

# 保证当前地图关键区块已加载
execute at @e[type=marker,tag=cs2_spawn_t,limit=1] run cs2chunks retain ~ ~
execute at @e[type=marker,tag=cs2_spawn_ct,limit=1] run cs2chunks retain ~ ~
execute at @e[type=marker,tag=cs2_site_a,limit=1] run cs2chunks retain ~ ~
execute at @e[type=marker,tag=cs2_site_b,limit=1] run cs2chunks retain ~ ~
execute at @e[type=marker,tag=cs2_lobby,limit=1] run cs2chunks retain ~ ~

$data modify storage cs2:map active set value "$(map)"
$tellraw @s {"text":"已切换到地图 $(map)。","color":"green"}
