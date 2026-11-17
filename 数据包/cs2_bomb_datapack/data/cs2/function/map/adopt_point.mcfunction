# 宏：把当前启用标签的 marker 坐标写入地图存储。
# 参数：map、point、tag
$data modify storage cs2:map points.$(map).$(point) set value {x:0,z:0}
$execute store result storage cs2:map points.$(map).$(point).x int 1 run data get entity @e[type=marker,tag=cs2_$(tag),limit=1] Pos[0]
$execute store result storage cs2:map points.$(map).$(point).z int 1 run data get entity @e[type=marker,tag=cs2_$(tag),limit=1] Pos[2]
