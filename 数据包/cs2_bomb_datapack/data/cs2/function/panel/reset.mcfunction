# 每场比赛重置战术暂停/投票状态
scoreboard players set #paused cs2_counts 0
scoreboard players set #pause_kind cs2_counts 0
scoreboard players set #pause_timer cs2_counts 0
scoreboard players set #pause_state cs2_counts 0
scoreboard players set #pause_side cs2_counts 0
scoreboard players set #mpause_t cs2_counts 0
scoreboard players set #mpause_c cs2_counts 0
scoreboard players set #pause_t cs2_counts 0
scoreboard players set #pause_c cs2_counts 0
function cs2:panel/reset_votes
