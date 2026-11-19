scoreboard players reset @s cs2_join_ct
scoreboard players enable @s cs2_join_ct
execute if score #state cs2_state matches 3..6 run return run function cs2:join/queue_request {side:2,label:"CT"}
execute if score #state cs2_state matches 2 if score #round_replayed cs2_counts matches 1 run return run function cs2:join/queue_request {side:2,label:"CT"}
execute if score #state cs2_state matches 2 run return run function cs2:join/freeze_ct
execute if score #state cs2_state matches 0..1 run return run function cs2:join/do_ct
execute if score #state cs2_state matches 7 run function cs2:join/do_ct
