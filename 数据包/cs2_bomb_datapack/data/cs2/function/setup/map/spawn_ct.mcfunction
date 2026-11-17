# 在玩家脚下设置指定地图的 CT 出生点：/function cs2:setup/map/spawn_ct {map:"dust"}
$kill @e[type=marker,tag=cs2_map_$(map)_spawn_ct]
$execute at @s run summon marker ~ ~ ~ {Tags:["cs2_map_$(map)_spawn_ct","cs2_map_marker"]}
$execute at @s run tp @e[type=marker,tag=cs2_map_$(map)_spawn_ct,limit=1] ~ ~ ~ ~ ~
execute at @s run forceload add ~ ~
$function cs2:setup/map/store {map:"$(map)",point:"spawn_ct"}
$tellraw @s {"text":"地图 $(map) 的 CT 出生点已保存。","color":"green"}
