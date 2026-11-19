# 在玩家脚下设置指定地图的大厅：/function cs2:setup/map/lobby {map:"dust"}
$kill @e[type=marker,tag=cs2_map_$(map)_lobby]
$execute at @s run summon marker ~ ~ ~ {Tags:["cs2_map_$(map)_lobby","cs2_map_marker"]}
$execute at @s run tp @e[type=marker,tag=cs2_map_$(map)_lobby,limit=1] ~ ~ ~ ~ ~
execute at @s run cs2chunks retain ~ ~
$function cs2:setup/map/store {map:"$(map)",point:"lobby"}
$tellraw @s {"text":"地图 $(map) 的大厅已保存。","color":"green"}
