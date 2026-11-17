# 给当前已经加载的所有地图点补齐区块强制加载。
# 用法：/function cs2:map/forceload_all
# 注意：区块完全卸载后，@e 找不到其中的 marker；需要先到该地图附近（让区块加载）再执行一次。
execute as @e[type=marker,tag=cs2_map_marker] at @s run forceload add ~ ~
tellraw @a {"text":"已为当前已加载的地图点补齐区块强制加载。","color":"green"}
