# 存活数
scoreboard players set #t_alive cs2_counts 0
scoreboard players set #ct_alive cs2_counts 0
execute as @a[team=cs2_t,scores={cs2_alive=1}] run scoreboard players add #t_alive cs2_counts 1
execute as @a[team=cs2_ct,scores={cs2_alive=1}] run scoreboard players add #ct_alive cs2_counts 1

# 秒数（动作栏使用；侧边栏已停用）
scoreboard players operation #timer_sec cs2_counts = #timer cs2_timer
scoreboard players operation #timer_sec cs2_counts /= #20 cs2_const
execute if score #timer_sec cs2_counts matches ..-1 run scoreboard players set #timer_sec cs2_counts 0
scoreboard players operation #bomb_sec cs2_counts = #bomb_timer cs2_bomb_timer
scoreboard players operation #bomb_sec cs2_counts /= #20 cs2_const
execute if score #bomb_sec cs2_counts matches ..-1 run scoreboard players set #bomb_sec cs2_counts 0

function cs2:hud/actionbar

# 播报目标：只给参赛者与解说
function cs2:util/feed_target
