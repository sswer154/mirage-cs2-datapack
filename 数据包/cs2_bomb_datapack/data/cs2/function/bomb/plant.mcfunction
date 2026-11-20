scoreboard players set #state cs2_state 4
scoreboard players set #bomb cs2_bomb_state 3
scoreboard players set @s cs2_planter 1
scoreboard players operation #bomb_timer cs2_bomb_timer = bomb cs2_cfg
scoreboard players operation #bomb_timer cs2_bomb_timer *= #20 cs2_const
scoreboard players set #drop_timer cs2_counts 0
# 记录安放时的人数劣势：安放方参赛人数比对方少 1 人及以上
scoreboard players set #plant_disadvantage cs2_counts 0
scoreboard players set #plant_ally cs2_counts 0
scoreboard players set #plant_enemy cs2_counts 0
execute if entity @s[team=cs2_t] as @a[team=cs2_t,scores={cs2_inmatch=1}] run scoreboard players add #plant_ally cs2_counts 1
execute if entity @s[team=cs2_t] as @a[team=cs2_ct,scores={cs2_inmatch=1}] run scoreboard players add #plant_enemy cs2_counts 1
execute if entity @s[team=cs2_ct] as @a[team=cs2_ct,scores={cs2_inmatch=1}] run scoreboard players add #plant_ally cs2_counts 1
execute if entity @s[team=cs2_ct] as @a[team=cs2_t,scores={cs2_inmatch=1}] run scoreboard players add #plant_enemy cs2_counts 1
scoreboard players operation #plant_diff cs2_counts = #plant_enemy cs2_counts
scoreboard players operation #plant_diff cs2_counts -= #plant_ally cs2_counts
execute if score #plant_diff cs2_counts matches 1.. run scoreboard players set #plant_disadvantage cs2_counts 1
scoreboard players set #blink cs2_blink 0
scoreboard players set #blink_on cs2_blink 0
scoreboard players set @s cs2_carrier 0
clear @s minecraft:nautilus_shell[minecraft:custom_data={cs2_bomb:1b}]
# 安放瞬间清零拆弹进度，并对齐 sneak_time 基线，避免把安放前的潜行时间算进拆弹
scoreboard players set @a[team=cs2_ct] cs2_defuse 0
execute as @a run scoreboard players operation @s cs2_sneak_prev = @s cs2_sneak

execute at @s run summon marker ~ ~ ~ {Tags:["cs2_bomb"]}
execute at @s run summon item_display ~ ~0.2 ~ {Tags:["cs2_c4_display"],item:{id:"minecraft:nautilus_shell",count:1,components:{"minecraft:custom_data":{cs2_bomb:1b},"minecraft:custom_model_data":721001}}}
# 隐形点击实体：略大于模型的矩形碰撞箱，右键它开始拆包
execute at @s run summon minecraft:armor_stand ~ ~0.2 ~ {Tags:["cs2_c4_hit"],Invisible:1b,NoGravity:1b,Invulnerable:1b,Silent:1b,DisabledSlots:2039583,ShowArms:0b,PersistenceRequired:1b}
execute at @s run attribute @e[type=armor_stand,tag=cs2_c4_hit,limit=1,sort=nearest,distance=..1] minecraft:generic.scale base set 0.7
execute at @s run data merge entity @e[type=item_display,tag=cs2_c4_display,limit=1] {transformation:{translation:[0f,0.3f,0f],scale:[1.2f,1.2f,1.2f],left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f]}}

scoreboard players set #boom_armed cs2_counts 0
bossbar set cs2:round visible false
bossbar set cs2:bomb visible true
execute store result bossbar cs2:bomb max run scoreboard players get #bomb_timer cs2_bomb_timer
execute store result bossbar cs2:bomb value run scoreboard players get #bomb_timer cs2_bomb_timer

function cs2:util/feed_target
title @a[tag=cs2_feed_target,scores={cs2_hud=1}] times 10 40 10
title @a[tag=cs2_feed_target,scores={cs2_hud=1}] subtitle {"text":""}
title @a[tag=cs2_feed_target,scores={cs2_hud=1}] title {"text":"炸弹已安放","color":"red","bold":true}
tellraw @a[tag=cs2_feed_target] [{"text":"炸弹已安放","color":"red"},{"text":"，","color":"gray"},{"score":{"name":"bomb","objective":"cs2_cfg"},"color":"yellow"},{"text":" 秒后爆炸。","color":"gray"}]
playsound minecraft:block.respawn_anchor.set_spawn master @a
# 安包提示音：只给参赛者与解说
scoreboard players set #c4_sfx cs2_sfx 1
particle minecraft:smoke ~ ~0.4 ~ 0.25 0.25 0.25 0.01 20 normal
particle minecraft:flame ~ ~0.4 ~ 0.15 0.15 0.15 0.01 8 normal
