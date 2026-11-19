# 对指定地图的五个已保存点执行 forceload；即使区块已经卸载也能工作。
# 用法：/function cs2:map/forceload_target {map:"dust"}
$function cs2:map/forceload_point {map:"$(map)",point:"spawn_t"}
$function cs2:map/forceload_point {map:"$(map)",point:"spawn_ct"}
$function cs2:map/forceload_point {map:"$(map)",point:"site_a"}
$function cs2:map/forceload_point {map:"$(map)",point:"site_b"}
$function cs2:map/forceload_point {map:"$(map)",point:"lobby"}
