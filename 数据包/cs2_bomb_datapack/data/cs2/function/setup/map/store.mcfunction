# 宏：保存当前玩家所在位置到指定地图/点的坐标存储。
# 由 setup/map/* 调用；参数：map、point
$data modify storage cs2:map points.$(map).$(point) set value {x:0,z:0}
$execute store result storage cs2:map points.$(map).$(point).x int 1 run data get entity @s Pos[0]
$execute store result storage cs2:map points.$(map).$(point).z int 1 run data get entity @s Pos[2]
