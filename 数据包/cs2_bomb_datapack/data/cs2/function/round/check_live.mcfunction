execute unless score #state cs2_state matches 3 run return 0
scoreboard players set #t_alive cs2_counts 0
scoreboard players set #ct_alive cs2_counts 0
execute as @a[team=cs2_t,scores={cs2_alive=1}] run scoreboard players add #t_alive cs2_counts 1
execute as @a[team=cs2_ct,scores={cs2_alive=1}] run scoreboard players add #ct_alive cs2_counts 1

execute if score #t_alive cs2_counts matches 0 unless score #solo cs2_counts matches 2 run scoreboard players set #reason cs2_counts 1
execute if score #t_alive cs2_counts matches 0 unless score #solo cs2_counts matches 2 run function cs2:round/end_ct
execute if score #state cs2_state matches 3 if score #ct_alive cs2_counts matches 0 unless score #solo cs2_counts matches 1 run scoreboard players set #reason cs2_counts 2
execute if score #state cs2_state matches 3 if score #ct_alive cs2_counts matches 0 unless score #solo cs2_counts matches 1 run function cs2:round/end_t
