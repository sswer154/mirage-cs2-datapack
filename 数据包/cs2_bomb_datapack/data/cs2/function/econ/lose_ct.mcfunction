# CT 失败：获得当前连败奖励并增加连败数
cs2econ team 2 from "#loss_ct"
scoreboard players add #loss_ct cs2_econ 500
execute if score #loss_ct cs2_econ matches 3401.. run scoreboard players set #loss_ct cs2_econ 3400
