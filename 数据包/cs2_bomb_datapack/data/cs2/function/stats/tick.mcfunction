# 每 tick 处理待播报的死亡与击杀者标签过期
execute as @a[scores={cs2_death_pending=1}] run function cs2:stats/death_feed
execute as @a[tag=cs2_recent_killer] run function cs2:stats/killer_tick
