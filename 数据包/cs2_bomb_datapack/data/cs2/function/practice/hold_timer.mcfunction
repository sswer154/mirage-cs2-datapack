# 训练模式：暂停准备阶段倒计时
scoreboard players reset @s cs2_train_hold
scoreboard players enable @s cs2_train_hold
execute unless score #solo cs2_counts matches 1..2 run tellraw @s {"text":"只有训练模式可以暂停回合倒计时。","color":"red"}
execute unless score #solo cs2_counts matches 1..2 run return 0
execute unless score #state cs2_state matches 2 run tellraw @s {"text":"只能在准备阶段暂停倒计时。","color":"red"}
execute unless score #state cs2_state matches 2 run return 0
scoreboard players set #train_hold cs2_counts 1
bossbar set cs2:round visible false
tellraw @s {"text":"回合倒计时已取消；再次使用战术菜单可继续训练或退出。","color":"yellow"}
