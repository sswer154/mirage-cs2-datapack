# CT 获胜：连败奖励 -1000，最低 1400
scoreboard players remove #loss_ct cs2_econ 1000
execute if score #loss_ct cs2_econ matches ..1399 run scoreboard players set #loss_ct cs2_econ 1400
