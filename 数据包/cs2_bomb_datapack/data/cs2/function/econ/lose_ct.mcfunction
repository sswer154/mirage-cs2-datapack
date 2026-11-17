# CT 失败：获得当前连败奖励并增加连败数
scoreboard players operation @a[scores={cs2_team=2}] cs2_money += #loss_ct cs2_econ
scoreboard players add #loss_ct cs2_econ 500
execute if score #loss_ct cs2_econ matches 3401.. run scoreboard players set #loss_ct cs2_econ 3400
