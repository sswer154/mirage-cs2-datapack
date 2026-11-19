# 每回合开始：清空本回合 CT 击杀数与安包标记
scoreboard players set #ct_kills cs2_econ 0
execute as @a[scores={cs2_inmatch=1}] run function cs2:stats/reset_round_highlights
execute as @a[scores={cs2_inmatch=1}] run function cs2:shop/reset_round_purchases
scoreboard players reset * cs2_planter
# 练习模式：每回合重新给 10000 资金
execute if score #solo cs2_counts matches 1..2 as @a[scores={cs2_inmatch=1}] run scoreboard players set @s cs2_money 10000
