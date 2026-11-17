# CT 获胜：拆包 3500，其余 3250
execute if score #reason cs2_counts matches 4 run scoreboard players add @a[scores={cs2_team=2}] cs2_money 3500
execute unless score #reason cs2_counts matches 4 run scoreboard players add @a[scores={cs2_team=2}] cs2_money 3250
