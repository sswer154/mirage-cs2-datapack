execute unless score #state cs2_state matches 4 run return 0
scoreboard players set #ct_alive cs2_counts 0
execute as @a[team=cs2_ct,scores={cs2_alive=1}] run scoreboard players add #ct_alive cs2_counts 1
execute if score #ct_alive cs2_counts matches 0 unless score #solo cs2_counts matches 1 run scoreboard players set #reason cs2_counts 6
execute if score #ct_alive cs2_counts matches 0 unless score #solo cs2_counts matches 1 run function cs2:round/end_t
