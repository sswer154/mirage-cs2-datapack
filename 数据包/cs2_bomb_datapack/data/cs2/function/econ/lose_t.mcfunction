# T 失败：获得当前连败奖励；时间耗尽失败不增加连败数
scoreboard players operation @a[scores={cs2_team=1}] cs2_money += #loss_t cs2_econ
execute unless score #reason cs2_counts matches 5 run scoreboard players add #loss_t cs2_econ 500
execute if score #loss_t cs2_econ matches 3401.. run scoreboard players set #loss_t cs2_econ 3400
