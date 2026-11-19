# 旧记录优先从已加载的实际标记补维度，找不到时按旧地图默认为主世界。
$execute unless data storage cs2:map points.$(map).$(point).dimension at @e[type=marker,tag=cs2_map_$(map)_$(point),limit=1] run cs2chunks remember "$(map)" "$(point)"
$cs2chunks load_point "$(map)" "$(point)"
