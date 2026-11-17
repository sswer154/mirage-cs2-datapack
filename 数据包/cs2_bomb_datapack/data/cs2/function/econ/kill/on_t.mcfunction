# 击杀了一名 T；K/D/播报由 cs2grenade 模组维护
execute unless score #state cs2_state matches 3..4 run return 0
execute if entity @s[team=cs2_ct] run function cs2:econ/kill/ct
