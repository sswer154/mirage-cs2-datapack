# 击杀了一名 CT（victim 带 cs2_tag_ct）
advancement revoke @s only cs2:econ/kill_ct
execute unless score #state cs2_state matches 3..4 run return 0
function cs2:stats/on_kill
execute if entity @s[team=cs2_ct] run tag @s add cs2_tk_recent
execute if entity @s[team=cs2_t] run function cs2:econ/kill/reward
