# 未入场的在线玩家立即取回背包，离线玩家重连时由退队流程返还。
execute as @a[scores={cs2_inmatch=0,cs2_queued_team=1..2}] run function cs2:join/cancel_queue
scoreboard players reset * cs2_queued_team
scoreboard players reset * cs2_queue_round
scoreboard players reset * cs2_cancel_queue
