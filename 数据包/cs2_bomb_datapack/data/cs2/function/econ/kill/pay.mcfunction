scoreboard players operation @s cs2_money += #kill_money cs2_econ
tellraw @s [{"text":"击杀奖励 ","color":"green"},{"text":"+$","color":"gold"},{"score":{"name":"#kill_money","objective":"cs2_econ"},"color":"gold"}]
