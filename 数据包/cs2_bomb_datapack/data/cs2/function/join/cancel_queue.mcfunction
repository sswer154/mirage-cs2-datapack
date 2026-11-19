scoreboard players reset @s cs2_cancel_queue
execute unless score @s cs2_queued_team matches 1..2 run return 0
execute unless score @s cs2_inmatch matches 1 run function cs2:join/to_lobby
function cs2:join/clear_queue
tellraw @s {"text":"已取消等待。","color":"gray"}
