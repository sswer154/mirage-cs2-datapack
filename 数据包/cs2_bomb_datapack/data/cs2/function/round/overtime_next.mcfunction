# 本组目标比分减一，就是两边各赢 3 回合时的总比分。
scoreboard players operation #ot_tie cs2_score_t = #target cs2_score_t
scoreboard players remove #ot_tie cs2_score_t 1
execute if score #t cs2_score_t = #ot_tie cs2_score_t if score #ct cs2_score_ct = #ot_tie cs2_score_t run return run function cs2:round/overtime_start

execute unless score #ot_half cs2_bomb_state matches 0 run return 0
scoreboard players set #6 cs2_const 6
scoreboard players operation #ot_round cs2_round = #round cs2_round
scoreboard players remove #ot_round cs2_round 24
scoreboard players operation #ot_round cs2_round %= #6 cs2_const
execute if score #ot_round cs2_round matches 3 run function cs2:round/overtime_halftime
