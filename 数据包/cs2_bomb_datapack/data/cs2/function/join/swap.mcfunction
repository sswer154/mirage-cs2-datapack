# 在线参赛者立即同步，离线参赛者在重连时补做。
scoreboard players add #swap cs2_side_epoch 1
execute as @a[scores={cs2_inmatch=1,cs2_team=1..2}] run function cs2:join/sync_side
