scoreboard players reset @s cs2_join_t
scoreboard players enable @s cs2_join_t
execute if score #state cs2_state matches 3 run tellraw @s {"text":"本回合已经开始，请等待下一回合再加入。","color":"red"}
execute if score #state cs2_state matches 4 run tellraw @s {"text":"本回合已经开始，请等待下一回合再加入。","color":"red"}
execute if score #state cs2_state matches 7 run tellraw @s {"text":"比赛已结束，请等待管理员开始新比赛。","color":"red"}
execute if score #state cs2_state matches 0 run function cs2:join/do_t
execute if score #state cs2_state matches 1 run function cs2:join/do_t
execute if score #state cs2_state matches 2 run function cs2:join/freeze_t
execute if score #state cs2_state matches 5 run function cs2:join/do_t
execute if score #state cs2_state matches 6 run function cs2:join/do_t
