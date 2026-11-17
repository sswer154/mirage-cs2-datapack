# 击杀了一名 CT；K/D/播报由 cs2grenade 模组维护
execute unless score #state cs2_state matches 3..4 run return 0
execute if entity @s[team=cs2_t] run function cs2:econ/kill/reward
