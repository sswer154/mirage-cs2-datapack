$execute if score #state cs2_state matches 0 run tellraw $(audience) {"text":"[CS2] 状态：空闲","color":"gray"}
$execute if score #state cs2_state matches 1 run tellraw $(audience) {"text":"[CS2] 状态：热身","color":"gold"}
$execute if score #state cs2_state matches 2 run tellraw $(audience) {"text":"[CS2] 状态：准备/冻结","color":"yellow"}
$execute if score #state cs2_state matches 3 run tellraw $(audience) {"text":"[CS2] 状态：进行中","color":"green"}
$execute if score #state cs2_state matches 4 run tellraw $(audience) {"text":"[CS2] 状态：炸弹已安放","color":"red"}
$execute if score #state cs2_state matches 5 run tellraw $(audience) {"text":"[CS2] 状态：回合结束","color":"yellow"}
$execute if score #state cs2_state matches 6 run tellraw $(audience) {"text":"[CS2] 状态：换边/加时休息","color":"aqua"}
$execute if score #state cs2_state matches 7 run tellraw $(audience) {"text":"[CS2] 状态：比赛结束","color":"gold"}
$execute if score #solo cs2_counts matches 1 run tellraw $(audience) {"text":"[CS2] 测试模式：仅 T 队","color":"yellow"}
$execute if score #solo cs2_counts matches 2 run tellraw $(audience) {"text":"[CS2] 测试模式：仅 CT 队","color":"yellow"}
$tellraw $(audience) [{"text":"[CS2] 第 ","color":"gray"},{"score":{"name":"#round","objective":"cs2_round"}},{"text":" 回合  比分  T ","color":"gray"},{"score":{"name":"#t","objective":"cs2_score_t"}},{"text":" : ","color":"white"},{"score":{"name":"#ct","objective":"cs2_score_ct"}},{"text":" CT  目标 ","color":"gray"},{"score":{"name":"#target","objective":"cs2_score_t"}}]
