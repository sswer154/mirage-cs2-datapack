# 从当前启用标记所在的位置和维度写入地图点位。
$execute at @e[type=marker,tag=cs2_$(tag),limit=1] run cs2chunks remember "$(map)" "$(point)"
