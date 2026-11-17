# 换边时只交换“比分”。装备/选择/权限都跟随玩家本人，不再交换。
# 必须在 join/swap 之后调用。
scoreboard players operation #swap_score cs2_score_t = #t cs2_score_t
scoreboard players operation #t cs2_score_t = #ct cs2_score_ct
scoreboard players operation #ct cs2_score_ct = #swap_score cs2_score_t

# 连败奖励也跟随队伍换边
scoreboard players operation #swap_loss cs2_econ = #loss_t cs2_econ
scoreboard players operation #loss_t cs2_econ = #loss_ct cs2_econ
scoreboard players operation #loss_ct cs2_econ = #swap_loss cs2_econ