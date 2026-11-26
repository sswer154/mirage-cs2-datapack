# 训练模式：暂停准备阶段倒计时
scoreboard players reset @s cs2_train_hold
scoreboard players enable @s cs2_train_hold
execute unless score #solo cs2_counts matches 1..2 run tellraw @s {"text":"只有训练模式可以暂停回合倒计时。","color":"red"}
execute unless score #solo cs2_counts matches 1..2 run return 0
execute unless score #state cs2_state matches 2 run tellraw @s {"text":"只能在准备阶段暂停倒计时。","color":"red"}
execute unless score #state cs2_state matches 2 run return 0
execute if score #train_hold cs2_counts matches 1 run return run function cs2:practice/resume_timer
scoreboard players set #train_hold cs2_counts 1
execute as @a[scores={cs2_inmatch=1}] run function cs2:util/freeze_unlock
bossbar set cs2:round visible false
tellraw @s {"text":"回合倒计时已暂停；可从战术菜单恢复。","color":"yellow"}
