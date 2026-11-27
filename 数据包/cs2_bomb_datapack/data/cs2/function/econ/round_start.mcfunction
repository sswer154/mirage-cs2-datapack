# 每回合开始：清空本回合 CT 击杀数与安包标记
scoreboard players set #ct_kills cs2_econ 0
execute as @a[scores={cs2_inmatch=1}] run function cs2:stats/reset_round_highlights
execute as @a[scores={cs2_inmatch=1}] run function cs2:shop/reset_round_purchases
scoreboard players reset * cs2_planter
# 练习模式：每回合重新给 10000 资金
execute if score #solo cs2_counts matches 1..2 as @a[scores={cs2_inmatch=1}] run scoreboard players set @s cs2_money 10000

# 加时新回合覆盖余额，离线期间累计的上一回合奖励也不再结转。
execute if score #ot cs2_bomb_state matches 1 unless score #round_replayed cs2_counts matches 1 run scoreboard players reset * cs2_pending_pay
execute if score #ot cs2_bomb_state matches 1 as @a[scores={cs2_inmatch=1,cs2_team=1..2}] run scoreboard players set @s cs2_money 9000
