# 训练模式：结束练习时清除训练靶并恢复准备倒计时状态
kill @e[type=minecraft:zombie,tag=cs2_training_dummy]
scoreboard players set #train_hold cs2_counts 0
