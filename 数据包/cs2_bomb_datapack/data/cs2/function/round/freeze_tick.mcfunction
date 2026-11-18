# 训练模式取消倒计时：保持准备阶段，不递减计时器
execute if score #train_hold cs2_counts matches 1 run return 0
scoreboard players remove #timer cs2_timer 1
execute if score #timer cs2_timer matches ..0 run scoreboard players set #timer cs2_timer 0
execute store result bossbar cs2:round value run scoreboard players get #timer cs2_timer
execute if score #timer cs2_timer matches 0 run function cs2:round/live
