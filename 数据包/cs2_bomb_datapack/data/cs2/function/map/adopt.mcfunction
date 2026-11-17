# 把当前已启用的五个配置点收编为命名地图：/function cs2:map/adopt {map:"dust"}
# 适用于升级前已经用 setup/spawn_t 等设置好、但还没有地图标签的存档。
execute unless entity @e[type=marker,tag=cs2_spawn_t,limit=1] run tellraw @s {"text":"当前没有启用的 T 出生点，无法收编。","color":"red"}
execute unless entity @e[type=marker,tag=cs2_spawn_t,limit=1] run return 0
execute unless entity @e[type=marker,tag=cs2_spawn_ct,limit=1] run tellraw @s {"text":"当前没有启用的 CT 出生点，无法收编。","color":"red"}
execute unless entity @e[type=marker,tag=cs2_spawn_ct,limit=1] run return 0
execute unless entity @e[type=marker,tag=cs2_site_a,limit=1] run tellraw @s {"text":"当前没有启用的 A 包点，无法收编。","color":"red"}
execute unless entity @e[type=marker,tag=cs2_site_a,limit=1] run return 0
execute unless entity @e[type=marker,tag=cs2_site_b,limit=1] run tellraw @s {"text":"当前没有启用的 B 包点，无法收编。","color":"red"}
execute unless entity @e[type=marker,tag=cs2_site_b,limit=1] run return 0
execute unless entity @e[type=marker,tag=cs2_lobby,limit=1] run tellraw @s {"text":"当前没有启用的大厅点，无法收编。","color":"red"}
execute unless entity @e[type=marker,tag=cs2_lobby,limit=1] run return 0

$tag @e[type=marker,tag=cs2_spawn_t] add cs2_map_$(map)_spawn_t
$tag @e[type=marker,tag=cs2_spawn_ct] add cs2_map_$(map)_spawn_ct
$tag @e[type=marker,tag=cs2_site_a] add cs2_map_$(map)_site_a
$tag @e[type=marker,tag=cs2_site_b] add cs2_map_$(map)_site_b
$tag @e[type=marker,tag=cs2_lobby] add cs2_map_$(map)_lobby
tag @e[type=marker,tag=cs2_spawn_t] add cs2_map_marker
tag @e[type=marker,tag=cs2_spawn_ct] add cs2_map_marker
tag @e[type=marker,tag=cs2_site_a] add cs2_map_marker
tag @e[type=marker,tag=cs2_site_b] add cs2_map_marker
tag @e[type=marker,tag=cs2_lobby] add cs2_map_marker
$tag @e[type=text_display,tag=cs2_site_a_text] add cs2_map_$(map)_site_a_text
$tag @e[type=text_display,tag=cs2_site_b_text] add cs2_map_$(map)_site_b_text
tag @e[type=text_display,tag=cs2_site_a_text] add cs2_map_text
tag @e[type=text_display,tag=cs2_site_b_text] add cs2_map_text
# 保存五个点的坐标，之后即使区块卸载也能自动 forceload。
$function cs2:map/adopt_point {map:"$(map)",point:"spawn_t",tag:"spawn_t"}
$function cs2:map/adopt_point {map:"$(map)",point:"spawn_ct",tag:"spawn_ct"}
$function cs2:map/adopt_point {map:"$(map)",point:"site_a",tag:"site_a"}
$function cs2:map/adopt_point {map:"$(map)",point:"site_b",tag:"site_b"}
$function cs2:map/adopt_point {map:"$(map)",point:"lobby",tag:"lobby"}
# 收编后立即给这些点补 forceload，避免下次切换时区块已卸载。
function cs2:map/forceload_all
$function cs2:map/forceload_target {map:"$(map)"}
$data modify storage cs2:map active set value "$(map)"
$tellraw @s {"text":"已把当前配置收编为地图 $(map)。","color":"green"}
