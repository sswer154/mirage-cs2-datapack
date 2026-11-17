# 比赛暂停没有计时，也不会自动结束
execute if score #pause_kind cs2_counts matches 2 run title @a[scores={cs2_inmatch=1,cs2_hud=1}] actionbar {"text":"比赛暂停 · 打开战术菜单可解除","color":"aqua"}
execute if score #pause_kind cs2_counts matches 2 run return 0
scoreboard players remove #pause_timer cs2_counts 1
execute store result bossbar cs2:round value run scoreboard players get #pause_timer cs2_counts
execute store result score #pause_sec cs2_counts run scoreboard players get #pause_timer cs2_counts
scoreboard players operation #pause_sec cs2_counts /= #20 cs2_const
title @a[scores={cs2_inmatch=1,cs2_hud=1}] actionbar [{"text":"战术暂停 ","color":"aqua"},{"score":{"name":"#pause_sec","objective":"cs2_counts"}},{"text":"s","color":"gray"}]
execute if score #pause_timer cs2_counts matches ..0 run function cs2:panel/resume
