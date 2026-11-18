# 每回合开始：清空本回合 CT 击杀数与安包标记
scoreboard players set #ct_kills cs2_econ 0
scoreboard players set @a[scores={cs2_inmatch=1}] cs2_round_kills 0
scoreboard players set @a[scores={cs2_inmatch=1}] cs2_fire_dmg 0
scoreboard players set @a[scores={cs2_inmatch=1}] cs2_grenade_dmg 0
scoreboard players set @a[scores={cs2_inmatch=1}] cs2_planter 0
scoreboard players set @a[scores={cs2_inmatch=1}] cs2_util_smoke 0
scoreboard players set @a[scores={cs2_inmatch=1}] cs2_util_he 0
scoreboard players set @a[scores={cs2_inmatch=1}] cs2_util_flash 0
scoreboard players set @a[scores={cs2_inmatch=1}] cs2_util_fire 0
# 练习模式：每回合重新给 10000 资金
execute if score #solo cs2_counts matches 1..2 as @a[scores={cs2_inmatch=1}] run scoreboard players set @s cs2_money 10000
scoreboard players set @a[scores={cs2_inmatch=1}] cs2_util_he_fast 0
scoreboard players set @a[scores={cs2_inmatch=1}] cs2_util_impact 0
scoreboard players set @a[scores={cs2_inmatch=1}] cs2_util_surgery 0
scoreboard players set @a[scores={cs2_inmatch=1}] cs2_util_golden 0
scoreboard players set @a[scores={cs2_inmatch=1}] cs2_item_count 0
