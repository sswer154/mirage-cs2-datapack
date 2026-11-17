# 用 cs2_seen 与全局 tick 的差值判断重连（1.21.1 没有 player_logged_in 触发器）
# 在线时每 tick 差值应为 1；2 以上说明玩家至少离线过一 tick
scoreboard players operation #now cs2_const = #tick cs2_const
scoreboard players operation #gap cs2_const = #now cs2_const
scoreboard players operation #gap cs2_const -= @s cs2_seen
execute if score #gap cs2_const matches 2.. run function cs2:player/on_join
scoreboard players operation @s cs2_seen = #now cs2_const
