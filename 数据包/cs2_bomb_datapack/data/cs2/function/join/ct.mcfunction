scoreboard players reset @s cs2_join_ct
scoreboard players enable @s cs2_join_ct
execute if score #state cs2_state matches 3 run tellraw @s {"text":"本回合已经开始，请等待下一回合再加入。","color":"red"}
execute if score #state cs2_state matches 4 run tellraw @s {"text":"本回合已经开始，请等待下一回合再加入。","color":"red"}
execute if score #state cs2_state matches 7 run function cs2:join/do_ct
execute if score #state cs2_state matches 0 run function cs2:join/do_ct
execute if score #state cs2_state matches 1 run function cs2:join/do_ct
execute if score #state cs2_state matches 2 run function cs2:join/freeze_ct
execute if score #state cs2_state matches 5 run function cs2:join/do_ct
execute if score #state cs2_state matches 6 run function cs2:join/do_ct
