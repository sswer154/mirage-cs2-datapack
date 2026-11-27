function cs2:player/round_reset
# 仅补做遗漏的回合初始化；同回合重连保留已消费后的余额。
execute if score #ot cs2_bomb_state matches 1 if score @s cs2_team matches 1..2 run scoreboard players set @s cs2_money 9000
# 准备阶段由 rejoin_freeze 传送；开打后补初始化的人仍在这里回出生点。
execute if score #state cs2_state matches 3..4 run function cs2:player/teleport_round_spawn
function cs2:stats/reset_round_highlights
function cs2:shop/reset_round_purchases
execute if score @s cs2_needs_default matches 1 if score @s cs2_team matches 1 run function cs2:shop/default_t
execute if score @s cs2_needs_default matches 1 if score @s cs2_team matches 2 run function cs2:shop/default_ct
scoreboard players set @s cs2_needs_default 0
execute if score #round_replayed cs2_counts matches 1 run cs2roundstate restore
function cs2:shop/snapshot_carry
cs2econ claim
cs2roundstate save
