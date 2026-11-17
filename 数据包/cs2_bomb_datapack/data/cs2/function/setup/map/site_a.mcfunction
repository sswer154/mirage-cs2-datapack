# 在玩家脚下设置指定地图的 A 包点：/function cs2:setup/map/site_a {map:"dust"}
$kill @e[type=marker,tag=cs2_map_$(map)_site_a]
$kill @e[type=text_display,tag=cs2_map_$(map)_site_a_text]
$execute at @s run summon marker ~ ~ ~ {Tags:["cs2_map_$(map)_site_a","cs2_map_marker"]}
$execute at @s run tp @e[type=marker,tag=cs2_map_$(map)_site_a,limit=1] ~ ~ ~ ~ ~
$execute at @s run summon text_display ~ ~2.5 ~ {Tags:["cs2_map_$(map)_site_a_text","cs2_map_text"],text:"{\"text\":\"\"}",billboard:"center",see_through:1b,background:0}
execute at @s run forceload add ~ ~
$function cs2:setup/map/store {map:"$(map)",point:"site_a"}
$tellraw @s {"text":"地图 $(map) 的 A 包点已保存（半径 10 格）。","color":"green"}
