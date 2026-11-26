scoreboard players set #train_hold cs2_counts 0
execute as @a[scores={cs2_inmatch=1}] run function cs2:util/freeze_lock
bossbar set cs2:round visible true
tellraw @s {"text":"回合倒计时已恢复。","color":"green"}
