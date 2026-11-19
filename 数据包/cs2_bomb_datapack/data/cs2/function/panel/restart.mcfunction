scoreboard players set #can_replay cs2_counts 1
execute as @a[scores={cs2_inmatch=1,cs2_team=1..2}] unless function cs2:round/has_checkpoint run scoreboard players set #can_replay cs2_counts 0
execute unless score #can_replay cs2_counts matches 1 run tellraw @a[scores={cs2_inmatch=1}] {"text":"本轮缺少开局记录，暂时不能重开。","color":"red"}
function cs2:panel/reset_votes
execute unless score #can_replay cs2_counts matches 1 run return fail
scoreboard players set #round_restarting cs2_counts 1
tellraw @a[scores={cs2_inmatch=1}] {"text":"双方同意，恢复开局装备和资金，重新开始本小局。","color":"green"}
function cs2:round/start
