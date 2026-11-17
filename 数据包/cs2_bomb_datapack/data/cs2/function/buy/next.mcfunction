# 回合结束后计算每名玩家下一回合可选的装备
# 由 round/end 在 #winner 设置后调用
execute if score #winner cs2_counts matches 1 run execute as @a[scores={cs2_team=1}] run function cs2:buy/next_win
execute if score #winner cs2_counts matches 1 run execute as @a[scores={cs2_team=2}] run function cs2:buy/next_lose
execute if score #winner cs2_counts matches 2 run execute as @a[scores={cs2_team=2}] run function cs2:buy/next_win
execute if score #winner cs2_counts matches 2 run execute as @a[scores={cs2_team=1}] run function cs2:buy/next_lose
execute if score #winner cs2_counts matches 0 run execute as @a[scores={cs2_team=1}] run function cs2:buy/next_lose
execute if score #winner cs2_counts matches 0 run execute as @a[scores={cs2_team=2}] run function cs2:buy/next_lose