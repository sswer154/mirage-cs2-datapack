# 对指定地图的五个已保存点执行 forceload；即使区块已经卸载也能工作。
# 用法：/function cs2:map/forceload_target {map:"dust"}
$function cs2:map/forceload_point with storage cs2:map points.$(map).spawn_t
$function cs2:map/forceload_point with storage cs2:map points.$(map).spawn_ct
$function cs2:map/forceload_point with storage cs2:map points.$(map).site_a
$function cs2:map/forceload_point with storage cs2:map points.$(map).site_b
$function cs2:map/forceload_point with storage cs2:map points.$(map).lobby
