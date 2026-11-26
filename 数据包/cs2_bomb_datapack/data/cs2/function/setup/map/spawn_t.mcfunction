# 在玩家脚下设置指定地图的 T 出生点：/function cs2:setup/map/spawn_t {map:"dust"}
$kill @e[type=marker,tag=cs2_map_$(map)_spawn_t,tag=!cs2_extra_spawn]
$execute at @s run summon marker ~ ~ ~ {Tags:["cs2_map_$(map)_spawn_t","cs2_map_marker"]}
$execute at @s run tp @e[type=marker,tag=cs2_map_$(map)_spawn_t,tag=!cs2_extra_spawn,limit=1] ~ ~ ~ ~ ~
execute at @s run cs2chunks retain ~ ~
$function cs2:setup/map/store {map:"$(map)",point:"spawn_t"}
$tellraw @s {"text":"地图 $(map) 的 T 出生点已保存。","color":"green"}
