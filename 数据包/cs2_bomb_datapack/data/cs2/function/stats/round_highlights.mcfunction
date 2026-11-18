# 回合结束高光播报：必须在 #bomb_timer 清零前调用
# 1) 本回合击杀最多，>=3 杀才播报
scoreboard players set #round_top_kills cs2_counts 0
execute as @a[scores={cs2_inmatch=1}] if score @s cs2_round_kills > #round_top_kills cs2_counts run scoreboard players operation #round_top_kills cs2_counts = @s cs2_round_kills
tag @a remove cs2_round_top
execute as @a[scores={cs2_inmatch=1}] if score @s cs2_round_kills = #round_top_kills cs2_counts if score #round_top_kills cs2_counts matches 3.. run tag @s add cs2_round_top
execute if score #round_top_kills cs2_counts matches 3.. run tellraw @a[scores={cs2_inmatch=1}] [{"text":"[高光] ","color":"gold"},{"text":"本回合击杀最多：","color":"yellow"},{"selector":"@a[tag=cs2_round_top]","color":"white"},{"text":"，杀死了 ","color":"gray"},{"score":{"name":"#round_top_kills","objective":"cs2_counts"},"color":"red"},{"text":" 个敌人。","color":"gray"}]

# 2) 拆包成功且剩余 <5 秒（<100 tick）
execute if score #reason cs2_counts matches 4 if score #bomb_timer cs2_bomb_timer matches ..99 run scoreboard players operation #defuse_sec cs2_counts = #bomb_timer cs2_bomb_timer
execute if score #reason cs2_counts matches 4 if score #bomb_timer cs2_bomb_timer matches ..99 run scoreboard players operation #defuse_sec cs2_counts /= #20 cs2_const
execute if score #reason cs2_counts matches 4 if score #bomb_timer cs2_bomb_timer matches ..99 run tellraw @a[scores={cs2_inmatch=1}] [{"text":"[高光] ","color":"gold"},{"text":"拆弹成功，剩余 ","color":"aqua"},{"score":{"name":"#defuse_sec","objective":"cs2_counts"},"color":"white"},{"text":" 秒。","color":"aqua"}]

# 3) 人数劣势下安放并引爆
execute if score #reason cs2_counts matches 3 if score #plant_disadvantage cs2_counts matches 1 run tellraw @a[scores={cs2_inmatch=1}] [{"text":"[高光] ","color":"gold"},{"text":"在人数劣势下完成安放并引爆。","color":"yellow"}]

# 4) 燃烧伤害最高，>20 点才播报
scoreboard players set #top_fire cs2_counts 0
execute as @a[scores={cs2_inmatch=1}] if score @s cs2_fire_dmg > #top_fire cs2_counts run scoreboard players operation #top_fire cs2_counts = @s cs2_fire_dmg
tag @a remove cs2_top_fire
execute as @a[scores={cs2_inmatch=1}] if score @s cs2_fire_dmg = #top_fire cs2_counts if score #top_fire cs2_counts matches 21.. run tag @s add cs2_top_fire
execute if score #top_fire cs2_counts matches 21.. run tellraw @a[scores={cs2_inmatch=1}] [{"text":"[高光] ","color":"gold"},{"text":"本回合燃烧伤害最高：","color":"yellow"},{"selector":"@a[tag=cs2_top_fire]","color":"white"},{"text":"，造成 ","color":"gray"},{"score":{"name":"#top_fire","objective":"cs2_counts"},"color":"red"},{"text":" 点燃烧伤害。","color":"gray"}]
# 5) 手雷伤害最高，>20 点才播报
scoreboard players set #top_grenade cs2_counts 0
execute as @a[scores={cs2_inmatch=1}] if score @s cs2_grenade_dmg > #top_grenade cs2_counts run scoreboard players operation #top_grenade cs2_counts = @s cs2_grenade_dmg
tag @a remove cs2_top_grenade
execute as @a[scores={cs2_inmatch=1}] if score @s cs2_grenade_dmg = #top_grenade cs2_counts if score #top_grenade cs2_counts matches 21.. run tag @s add cs2_top_grenade
execute if score #top_grenade cs2_counts matches 21.. run tellraw @a[scores={cs2_inmatch=1}] [{"text":"[高光] ","color":"gold"},{"text":"本回合手雷伤害最高：","color":"yellow"},{"selector":"@a[tag=cs2_top_grenade]","color":"white"},{"text":"，造成 ","color":"gray"},{"score":{"name":"#top_grenade","objective":"cs2_counts"},"color":"red"},{"text":" 点手雷伤害。","color":"gray"}]

tag @a remove cs2_round_top
tag @a remove cs2_top_fire
tag @a remove cs2_top_grenade
