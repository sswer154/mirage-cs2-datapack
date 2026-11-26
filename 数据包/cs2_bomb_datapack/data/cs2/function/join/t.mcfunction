scoreboard players reset @s cs2_join_t
scoreboard players enable @s cs2_join_t
execute unless function cs2:fairplay/join_t run return fail
execute if score #state cs2_state matches 3..6 run return run function cs2:join/queue_request {side:1,label:"T"}
execute if score #state cs2_state matches 2 if score #round_replayed cs2_counts matches 1 run return run function cs2:join/queue_request {side:1,label:"T"}
execute if score #state cs2_state matches 2 if entity @s[team=cs2_t,scores={cs2_inmatch=1}] run return run tellraw @s {"text":"你已经在 T 队。","color":"gray"}
execute if score #state cs2_state matches 2 run return run function cs2:join/freeze_t
execute if score #state cs2_state matches 0..1 run return run function cs2:join/do_t
execute if score #state cs2_state matches 7 run function cs2:join/do_t
