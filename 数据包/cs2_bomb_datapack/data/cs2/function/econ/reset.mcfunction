# 整场经济重置（在 match/reset 中调用）
scoreboard players reset * cs2_pending_pay
scoreboard players set #loss_t cs2_econ 1900
scoreboard players set #loss_ct cs2_econ 1900
scoreboard players set #ct_kills cs2_econ 0
scoreboard players set #kill_money cs2_econ 0
scoreboard players set #ct_bonus cs2_econ 0

scoreboard players set @a[scores={cs2_inmatch=1}] cs2_money 0
scoreboard players set @a[scores={cs2_inmatch=1}] cs2_primary 0
scoreboard players set @a[scores={cs2_inmatch=1}] cs2_secondary 0
scoreboard players set @a[scores={cs2_inmatch=1}] cs2_armor 0
scoreboard players set @a[scores={cs2_inmatch=1}] cs2_planter 0
scoreboard players set @a[scores={cs2_inmatch=1}] cs2_wclass 1
