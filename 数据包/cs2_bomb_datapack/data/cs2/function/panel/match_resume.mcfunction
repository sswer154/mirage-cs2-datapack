# 解除比赛暂停：任意参赛者均可，仅在比赛暂停中有效
scoreboard players reset @s cs2_match_resume
scoreboard players enable @s cs2_match_resume
execute unless score #paused cs2_counts matches 1 run tellraw @s {"text":"当前没有暂停。","color":"red"}
execute unless score #paused cs2_counts matches 1 run return 0
execute unless score #pause_kind cs2_counts matches 2 run tellraw @s {"text":"当前不是比赛暂停；战术暂停会自动结束。","color":"red"}
execute unless score #pause_kind cs2_counts matches 2 run return 0
execute unless score @s cs2_inmatch matches 1 run tellraw @s {"text":"你不是本场参赛者。","color":"red"}
execute unless score @s cs2_inmatch matches 1 run return 0
tellraw @a[scores={cs2_inmatch=1}] {"text":"比赛暂停已由玩家解除。","color":"green"}
function cs2:panel/resume
