# C4 已回收：若地面没有 C4，5 秒后补给存活的 T
execute if entity @e[type=item,tag=cs2_bomb_item] run return 0
execute unless entity @a[team=cs2_t,scores={cs2_inmatch=1,cs2_alive=1}] run return 0
scoreboard players remove #drop_timer cs2_counts 1
execute if score #drop_timer cs2_counts matches ..0 run function cs2:bomb/regive
