# 回合结束高光：先算各项候选，再随机抽一个播报
# 必须在 #bomb_timer 清零前调用

# 1) 本回合击杀最多，>=3 杀才候选
scoreboard players set #round_top_kills cs2_counts 0
execute as @a[scores={cs2_inmatch=1}] if score @s cs2_round_kills > #round_top_kills cs2_counts run scoreboard players operation #round_top_kills cs2_counts = @s cs2_round_kills
tag @a remove cs2_round_top
execute as @a[scores={cs2_inmatch=1}] if score @s cs2_round_kills = #round_top_kills cs2_counts if score #round_top_kills cs2_counts matches 3.. run tag @s add cs2_round_top

# 2) 燃烧/手雷伤害最高，>20 点才候选
scoreboard players set #top_fire cs2_counts 0
execute as @a[scores={cs2_inmatch=1}] if score @s cs2_fire_dmg > #top_fire cs2_counts run scoreboard players operation #top_fire cs2_counts = @s cs2_fire_dmg
tag @a remove cs2_top_fire
execute as @a[scores={cs2_inmatch=1}] if score @s cs2_fire_dmg = #top_fire cs2_counts if score #top_fire cs2_counts matches 21.. run tag @s add cs2_top_fire

scoreboard players set #top_grenade cs2_counts 0
execute as @a[scores={cs2_inmatch=1}] if score @s cs2_grenade_dmg > #top_grenade cs2_counts run scoreboard players operation #top_grenade cs2_counts = @s cs2_grenade_dmg
tag @a remove cs2_top_grenade
execute as @a[scores={cs2_inmatch=1}] if score @s cs2_grenade_dmg = #top_grenade cs2_counts if score #top_grenade cs2_counts matches 21.. run tag @s add cs2_top_grenade

# 3) 计算候选标记
scoreboard players set #hl_kill cs2_counts 0
scoreboard players set #hl_defuse cs2_counts 0
scoreboard players set #hl_plant cs2_counts 0
scoreboard players set #hl_fire cs2_counts 0
scoreboard players set #hl_grenade cs2_counts 0
execute if score #round_top_kills cs2_counts matches 3.. if entity @a[tag=cs2_round_top,limit=1] run scoreboard players set #hl_kill cs2_counts 1
execute if score #reason cs2_counts matches 4 if score #bomb_timer cs2_bomb_timer matches ..99 if entity @a[tag=cs2_last_defuser,limit=1] run scoreboard players set #hl_defuse cs2_counts 1
execute if score #reason cs2_counts matches 3 if score #plant_disadvantage cs2_counts matches 1 if entity @a[scores={cs2_planter=1},limit=1] run scoreboard players set #hl_plant cs2_counts 1
execute if score #top_fire cs2_counts matches 21.. if entity @a[tag=cs2_top_fire,limit=1] run scoreboard players set #hl_fire cs2_counts 1
execute if score #top_grenade cs2_counts matches 21.. if entity @a[tag=cs2_top_grenade,limit=1] run scoreboard players set #hl_grenade cs2_counts 1

# 4) 拆包精确到 0.01 秒：tick -> 百分秒
execute if score #hl_defuse cs2_counts matches 1 run scoreboard players operation #defuse_cs cs2_counts = #bomb_timer cs2_bomb_timer
execute if score #hl_defuse cs2_counts matches 1 run scoreboard players operation #defuse_cs cs2_counts *= #5 cs2_const
execute if score #hl_defuse cs2_counts matches 1 run scoreboard players operation #defuse_sec cs2_counts = #defuse_cs cs2_counts
execute if score #hl_defuse cs2_counts matches 1 run scoreboard players operation #defuse_sec cs2_counts /= #100 cs2_const
execute if score #hl_defuse cs2_counts matches 1 run scoreboard players operation #defuse_rem cs2_counts = #defuse_cs cs2_counts
execute if score #hl_defuse cs2_counts matches 1 run scoreboard players operation #defuse_rem cs2_counts %= #100 cs2_const

# 5) 随机抽一个候选；若抽到的候选未触发，回退到第一个已触发项
execute store result score #hl_pick cs2_counts run random value 1..5
scoreboard players set #hl_choice cs2_counts 0
execute if score #hl_pick cs2_counts matches 1 if score #hl_kill cs2_counts matches 1 run scoreboard players set #hl_choice cs2_counts 1
execute if score #hl_pick cs2_counts matches 2 if score #hl_defuse cs2_counts matches 1 run scoreboard players set #hl_choice cs2_counts 2
execute if score #hl_pick cs2_counts matches 3 if score #hl_plant cs2_counts matches 1 run scoreboard players set #hl_choice cs2_counts 3
execute if score #hl_pick cs2_counts matches 4 if score #hl_fire cs2_counts matches 1 run scoreboard players set #hl_choice cs2_counts 4
execute if score #hl_pick cs2_counts matches 5 if score #hl_grenade cs2_counts matches 1 run scoreboard players set #hl_choice cs2_counts 5
execute if score #hl_choice cs2_counts matches 0 if score #hl_kill cs2_counts matches 1 run scoreboard players set #hl_choice cs2_counts 1
execute if score #hl_choice cs2_counts matches 0 if score #hl_defuse cs2_counts matches 1 run scoreboard players set #hl_choice cs2_counts 2
execute if score #hl_choice cs2_counts matches 0 if score #hl_plant cs2_counts matches 1 run scoreboard players set #hl_choice cs2_counts 3
execute if score #hl_choice cs2_counts matches 0 if score #hl_fire cs2_counts matches 1 run scoreboard players set #hl_choice cs2_counts 4
execute if score #hl_choice cs2_counts matches 0 if score #hl_grenade cs2_counts matches 1 run scoreboard players set #hl_choice cs2_counts 5

execute if score #hl_choice cs2_counts matches 1 run function cs2:stats/highlight_mvp
execute if score #hl_choice cs2_counts matches 2 run function cs2:stats/highlight_defuse
execute if score #hl_choice cs2_counts matches 3 run function cs2:stats/highlight_plant
execute if score #hl_choice cs2_counts matches 4 run function cs2:stats/highlight_fire
execute if score #hl_choice cs2_counts matches 5 run function cs2:stats/highlight_grenade

tag @a remove cs2_round_top
tag @a remove cs2_top_fire
tag @a remove cs2_top_grenade
tag @a remove cs2_last_defuser
