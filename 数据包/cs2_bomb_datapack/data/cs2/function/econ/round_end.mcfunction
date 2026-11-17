# 回合结算：必须在 #winner/#reason 已设置、#bomb 尚未清零时调用
execute if score #winner cs2_counts matches 1 run function cs2:econ/win_t
execute if score #winner cs2_counts matches 2 run function cs2:econ/win_ct

# 安包者奖励 300（即使 T 最终输掉）
execute as @a[scores={cs2_planter=1}] run function cs2:econ/planter

# T 安包后输掉：每名 T 额外 600
execute if score #winner cs2_counts matches 2 if score #bomb cs2_bomb_state matches 3 run function cs2:econ/t_plant_loss

# CT 团队击杀奖励：每击杀 1 名 T，全队 50
execute if score #ct_kills cs2_econ matches 1.. run function cs2:econ/ct_kill_bonus

# 败方连败奖励
execute if score #winner cs2_counts matches 1 run function cs2:econ/lose_ct
execute if score #winner cs2_counts matches 2 run function cs2:econ/lose_t

# 胜方连败奖励递减 1000，最低 1400
execute if score #winner cs2_counts matches 1 run function cs2:econ/win_loss_t
execute if score #winner cs2_counts matches 2 run function cs2:econ/win_loss_ct
