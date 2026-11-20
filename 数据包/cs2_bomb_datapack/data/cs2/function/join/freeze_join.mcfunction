execute if score @s cs2_team matches 1 run function cs2:join/freeze_t
execute if score @s cs2_team matches 2 run function cs2:join/freeze_ct
execute if score @s cs2_team matches 0 run return run function cs2:join/to_lobby
execute unless entity @s[scores={cs2_inmatch=1,cs2_team=1..2,cs2_needs_default=0}] run return 0
function cs2:shop/ensure_ready
tellraw @s {"text":"已重新连接，准备阶段继续。","color":"green"}
