# 每小局开始时给本人显示一次本场 K/D
tellraw @s [{"text":"第 ","color":"gold"},{"score":{"name":"#round","objective":"cs2_round"}},{"text":" 回合  |  本场 K/D  ","color":"gray"},{"score":{"name":"@s","objective":"cs2_kills"},"color":"green"},{"text":" / ","color":"gray"},{"score":{"name":"@s","objective":"cs2_deaths"},"color":"red"}]
