# 比赛暂停：准备阶段双方各至少一人同意；无时间限制，任意参赛者随时解除
scoreboard players reset @s cs2_match_pause
scoreboard players enable @s cs2_match_pause
execute if score #paused cs2_counts matches 1 run tellraw @s {"text":"当前已经在暂停中。","color":"red"}
execute if score #paused cs2_counts matches 1 run return 0
execute unless score #state cs2_state matches 2 run tellraw @s {"text":"比赛暂停只能在准备阶段发起。","color":"red"}
execute unless score #state cs2_state matches 2 run return 0
execute unless score #solo cs2_counts matches 0 run tellraw @s {"text":"比赛暂停需要双方都有玩家，练习模式不能使用。","color":"red"}
execute unless score #solo cs2_counts matches 0 run return 0
execute unless score @s cs2_inmatch matches 1 run tellraw @s {"text":"你不是本场参赛者。","color":"red"}
execute unless score @s cs2_inmatch matches 1 run return 0
execute if score @s cs2_team matches 1 run scoreboard players set #mpause_t cs2_counts 1
execute if score @s cs2_team matches 2 run scoreboard players set #mpause_c cs2_counts 1
tellraw @a[scores={cs2_inmatch=1}] [{"text":"比赛暂停投票：","color":"aqua"},{"text":"T ","color":"red"},{"score":{"name":"#mpause_t","objective":"cs2_counts"}},{"text":"/1  CT ","color":"blue"},{"score":{"name":"#mpause_c","objective":"cs2_counts"}},{"text":"/1","color":"blue"},{"text":"（双方各一人同意即生效）","color":"gray"}]
execute if score #mpause_t cs2_counts matches 1 if score #mpause_c cs2_counts matches 1 run function cs2:panel/match_pause_start
