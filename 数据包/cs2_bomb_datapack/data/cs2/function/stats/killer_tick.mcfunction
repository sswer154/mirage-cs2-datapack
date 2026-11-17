# 击杀者标签 5 秒后自动过期，避免下一次死亡误配
scoreboard players remove @s cs2_killer_timer 1
execute if score @s cs2_killer_timer matches ..0 run tag @s remove cs2_recent_killer
