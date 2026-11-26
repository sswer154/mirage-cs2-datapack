# 开始一场比赛
execute unless function cs2:fairplay/check_start run return fail

execute unless entity @e[type=marker,tag=cs2_spawn_t] run tellraw @a {"text":"无法开始：缺少 T 出生点。","color":"red"}
execute unless entity @e[type=marker,tag=cs2_spawn_t] run return 0
execute unless entity @e[type=marker,tag=cs2_spawn_ct] run tellraw @a {"text":"无法开始：缺少 CT 出生点。","color":"red"}
execute unless entity @e[type=marker,tag=cs2_spawn_ct] run return 0
execute unless entity @e[type=marker,tag=cs2_site_a] run tellraw @a {"text":"无法开始：缺少 A 包点。","color":"red"}
execute unless entity @e[type=marker,tag=cs2_site_a] run return 0
execute unless entity @e[type=marker,tag=cs2_site_b] run tellraw @a {"text":"无法开始：缺少 B 包点。","color":"red"}
execute unless entity @e[type=marker,tag=cs2_site_b] run return 0
execute unless entity @e[type=marker,tag=cs2_lobby] run tellraw @a {"text":"无法开始：缺少大厅出生点。","color":"red"}
execute unless entity @e[type=marker,tag=cs2_lobby] run return 0

execute unless entity @a[team=cs2_t] unless entity @a[team=cs2_ct] run tellraw @a {"text":"无法开始：至少需要一名玩家加入 T 或 CT。","color":"red"}
execute unless entity @a[team=cs2_t] unless entity @a[team=cs2_ct] run return 0

gamerule keepInventory true

# 开赛时把五个关键点所在区块全部强加载，避免比赛途中区块卸载导致
# match/end 找不到大厅 marker、或 round/start 找不到出生点而把玩家留在原地。
execute at @e[type=marker,tag=cs2_spawn_t,limit=1] run cs2chunks retain ~ ~
execute at @e[type=marker,tag=cs2_spawn_ct,limit=1] run cs2chunks retain ~ ~
execute at @e[type=marker,tag=cs2_site_a,limit=1] run cs2chunks retain ~ ~
execute at @e[type=marker,tag=cs2_site_b,limit=1] run cs2chunks retain ~ ~
execute at @e[type=marker,tag=cs2_lobby,limit=1] run cs2chunks retain ~ ~

# 以队伍成员关系为准同步 cs2_team，避免旧存档/手动 /team join 造成队伍与分数不一致
# 未加入任何队伍的玩家不再属于旁观队伍；开始时只标记 T/CT 参赛者
execute as @a[team=cs2_t] run scoreboard players set @s cs2_team 1
execute as @a[team=cs2_ct] run scoreboard players set @s cs2_team 2

# 先清理旧存档中残留的参赛资格，再登记本场在线参赛者。
scoreboard players set * cs2_inmatch 0
execute as @a[team=cs2_t] if function cs2:player/save_inventory run scoreboard players set @s cs2_inmatch 1
execute as @a[team=cs2_ct] if function cs2:player/save_inventory run scoreboard players set @s cs2_inmatch 1

# 新比赛从当前阵营开始重新计算换边次数。
scoreboard players reset * cs2_side_epoch
scoreboard players set #swap cs2_side_epoch 0
scoreboard players set #halftime cs2_side_epoch 0
scoreboard players set #side_mod cs2_side_epoch 2
scoreboard players set #transition cs2_formal_epoch 0
execute unless entity @a[scores={cs2_inmatch=1}] run return fail
scoreboard players reset * cs2_ready_team
function cs2:join/end_queue
function cs2:match/reset
function cs2:stats/reset
function cs2:econ/start
execute unless entity @a[team=cs2_t,scores={cs2_inmatch=1}] run scoreboard players set #solo cs2_counts 2
execute unless entity @a[team=cs2_ct,scores={cs2_inmatch=1}] run scoreboard players set #solo cs2_counts 1
scoreboard players set #round cs2_round 1
scoreboard players set #target cs2_score_t 13
scoreboard players set #state cs2_state 1
cs2audit_lifecycle begin
function cs2:pvp/off
scoreboard players operation #timer cs2_timer = warmup cs2_cfg
scoreboard players operation #timer cs2_timer *= #20 cs2_const
scoreboard players set #bomb cs2_bomb_state 0
scoreboard players set #bomb_timer cs2_bomb_timer 0
scoreboard players set #blink cs2_blink 0
scoreboard players set #blink_on cs2_blink 0
scoreboard players set #break_type cs2_counts 0

bossbar set cs2:round visible true
execute store result bossbar cs2:round max run scoreboard players get #timer cs2_timer
execute store result bossbar cs2:round value run scoreboard players get #timer cs2_timer
bossbar set cs2:bomb visible false

execute as @a[scores={cs2_inmatch=1}] run function cs2:match/warmup_player
function cs2:util/feed_target
title @a[tag=cs2_feed_target] times 10 40 10
title @a[tag=cs2_feed_target,scores={cs2_hud=1}] title {"text":"比赛开始","color":"gold","bold":true}
title @a[tag=cs2_feed_target,scores={cs2_hud=1}] subtitle {"text":"热身阶段","color":"gray"}
tellraw @a[tag=cs2_feed_target] {"text":"比赛开始：热身结束后自动进入第 1 回合。","color":"gold"}
execute if score #solo cs2_counts matches 1 run tellraw @a[tag=cs2_feed_target] {"text":"测试模式：仅 T 队有玩家，CT 空位不会自动判负。","color":"yellow"}
execute if score #solo cs2_counts matches 2 run tellraw @a[tag=cs2_feed_target] {"text":"测试模式：仅 CT 队有玩家，T 空位不会自动判负。","color":"yellow"}
function cs2:match/status_broadcast
