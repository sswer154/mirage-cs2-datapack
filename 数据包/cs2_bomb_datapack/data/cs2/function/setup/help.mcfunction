tellraw @s {"text":"===== CS2 爆破模式 =====","color":"gold","bold":true}
tellraw @s {"text":"1. 站在 T 出生点执行 /function cs2:setup/spawn_t","color":"gray"}
tellraw @s {"text":"2. 站在 CT 出生点执行 /function cs2:setup/spawn_ct","color":"gray"}
tellraw @s {"text":"3. 站在 A 包点执行 /function cs2:setup/site_a","color":"gray"}
tellraw @s {"text":"4. 站在 B 包点执行 /function cs2:setup/site_b","color":"gray"}
tellraw @s {"text":"5. 站在大厅执行 /function cs2:setup/lobby","color":"gray"}
tellraw @s {"text":"6. 用 /function cs2:setup/check 检查","color":"gray"}
tellraw @s {"text":"7. 玩家用 /trigger cs2_join_t 或 cs2_join_ct 加入","color":"gray"}
tellraw @s {"text":"8. 管理员 /function cs2:match/start 开始","color":"gray"}
tellraw @s {"text":"--- 多地图 ---","color":"gold"}
tellraw @s {"text":"建图: /function cs2:setup/map/spawn_t {map:\"dust\"}","color":"gray"}
tellraw @s {"text":"      spawn_ct / site_a / site_b / lobby 同理","color":"gray"}
tellraw @s {"text":"切换: /function cs2:map/switch {map:\"dust\"}","color":"gray"}
tellraw @s {"text":"检查: /function cs2:map/check {map:\"dust\"}","color":"gray"}
tellraw @s {"text":"收编旧配置: /function cs2:map/adopt {map:\"旧地图名\"}","color":"gray"}
tellraw @s {"text":"状态: /function cs2:map/status","color":"gray"}
tellraw @s {"text":"停止: /function cs2:match/stop  状态: /function cs2:match/status","color":"yellow"}
