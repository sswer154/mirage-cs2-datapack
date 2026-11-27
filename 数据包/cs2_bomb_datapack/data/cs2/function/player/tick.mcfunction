# 原版退队不更新 CS2 分数。先处理，再进入重连或回合初始化。
execute if score @s cs2_team matches 1..2 if entity @s[team=!cs2_t] if entity @s[team=!cs2_ct] if function cs2:player/has_saved_inventory run function cs2:join/leave
function cs2:player/check_seen
function cs2:player/check_death
function cs2:player/sync_scale
