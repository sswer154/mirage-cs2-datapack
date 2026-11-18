# 计时归零：先放出爆炸声（音频开头有 0.9 秒余音，boom 在 0.9 秒处）
# 等 38 tick（1.9 秒），让 boom 那一瞬和伤害结算对齐
execute if score #boom_armed cs2_counts matches 0 run scoreboard players set #c4_sfx cs2_sfx 6
execute if score #boom_armed cs2_counts matches 0 run scoreboard players set #boom_armed cs2_counts 39
execute if score #boom_armed cs2_counts matches 0 run scoreboard players set #bomb_timer cs2_bomb_timer 1
scoreboard players remove #boom_armed cs2_counts 1
execute if score #boom_armed cs2_counts matches ..0 run function cs2:bomb/explode
