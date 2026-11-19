function cs2:practice/cleanup
scoreboard players set #solo cs2_counts 0
scoreboard players operation #transition cs2_formal_epoch = #epoch cs2_health_round
function cs2:match/reset
scoreboard players set #round cs2_round 1
execute as @a[scores={cs2_inmatch=1,cs2_team=1..2}] run function cs2:practice/formal_player
tellraw @a[scores={cs2_inmatch=1}] {"text":"双方已到齐。已清理练习装备，双方以 $800 重新进入准备阶段。","color":"gold"}
function cs2:round/start
