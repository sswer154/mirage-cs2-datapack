# 下半场换边后，重置经济并记录离线玩家需要补做的中场重置。
scoreboard players operation #halftime cs2_side_epoch = #swap cs2_side_epoch
function cs2:econ/reset
execute as @a[scores={cs2_inmatch=1}] run function cs2:econ/halftime_player
