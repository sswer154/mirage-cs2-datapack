# 本回合 CT 每击杀 1 名 T，全队 +50
scoreboard players operation #ct_bonus cs2_econ = #ct_kills cs2_econ
scoreboard players operation #ct_bonus cs2_econ *= #50 cs2_const
scoreboard players operation @a[scores={cs2_team=2}] cs2_money += #ct_bonus cs2_econ
tellraw @a[scores={cs2_team=2}] [{"text":"团队击杀奖励 ","color":"green"},{"text":"+$","color":"gold"},{"score":{"name":"#ct_bonus","objective":"cs2_econ"},"color":"gold"}]
