# 只有 deathCount 相对上一 tick 增长、且本 tick 仍存活时，才算一次新死亡
# 不能无条件设置 pending，否则每 tick 都会播报“阵亡”
execute if score @s cs2_deaths > @s cs2_prev_deaths if score @s cs2_inmatch matches 1 if score @s cs2_alive matches 1 run scoreboard players set @s cs2_death_pending 1
scoreboard players operation @s cs2_prev_deaths = @s cs2_deaths
execute unless score @s cs2_alive matches 1 run return 0
# 比赛中任何阶段死亡都要处理：死亡后重生不会再保留存活标记，
# 否则下一回合会被 round_reset 当作“上局存活”，既不清包也不发默认装备。
execute if score #state cs2_state matches 1 run function cs2:player/on_death
execute if score #state cs2_state matches 2 run function cs2:player/on_death
execute if score #state cs2_state matches 3 run function cs2:player/on_death
execute if score #state cs2_state matches 4 run function cs2:player/on_death
execute if score #state cs2_state matches 5 run function cs2:player/on_death
execute if score #state cs2_state matches 6 run function cs2:player/on_death
