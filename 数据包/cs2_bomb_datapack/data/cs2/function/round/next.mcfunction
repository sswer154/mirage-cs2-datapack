execute unless score #state cs2_state matches 5 run return 0

# 先判断是否达到目标分
execute if score #t cs2_score_t >= #target cs2_score_t run function cs2:match/end
execute if score #state cs2_state matches 7 run return 0
execute if score #ct cs2_score_ct >= #target cs2_score_t run function cs2:match/end
execute if score #state cs2_state matches 7 run return 0

# 第 12 回合结束换边
execute if score #round cs2_round matches 12 if score #half cs2_bomb_state matches 0 run function cs2:round/halftime
execute if score #state cs2_state matches 6 run return 0

# 第 24 回合：平局进入加时或直接平局
execute if score #round cs2_round matches 24 if score #t cs2_score_t = #ct cs2_score_ct if score overtime cs2_cfg matches 0 run function cs2:match/end
execute if score #state cs2_state matches 7 run return 0
execute if score #round cs2_round matches 24 if score #t cs2_score_t = #ct cs2_score_ct if score overtime cs2_cfg matches 1 run function cs2:round/overtime_start
execute if score #state cs2_state matches 6 run return 0

# 加时第 3 回合结束换边
execute if score #ot cs2_bomb_state matches 1 if score #round cs2_round matches 27 if score #ot_half cs2_bomb_state matches 0 run function cs2:round/overtime_halftime
execute if score #state cs2_state matches 6 run return 0

# 下一回合（15-15 时第 31 回合为突然死亡）
scoreboard players add #round cs2_round 1
function cs2:round/start
