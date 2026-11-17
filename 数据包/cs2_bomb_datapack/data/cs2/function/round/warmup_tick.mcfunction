# 热身倒计时
scoreboard players remove #timer cs2_timer 1
execute if score #timer cs2_timer matches ..0 run scoreboard players set #timer cs2_timer 0
execute store result bossbar cs2:round value run scoreboard players get #timer cs2_timer
execute if score #timer cs2_timer matches 0 run function cs2:round/start
