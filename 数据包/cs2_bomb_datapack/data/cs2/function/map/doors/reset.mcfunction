# 回合开始时重置当前地图的可操作门。
# 只重置明确配置过的地图；active 为空或未知地图时不做事。
execute if data storage cs2:map {active:"nuke"} run function cs2:map/doors/nuke
execute if data storage cs2:map {active:"overpass"} run function cs2:map/doors/overpass
execute if data storage cs2:map {active:"inferno"} run function cs2:map/doors/inferno
