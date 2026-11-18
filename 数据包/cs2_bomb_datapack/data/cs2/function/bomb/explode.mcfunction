execute unless score #state cs2_state matches 4 run return 0
scoreboard players set #boom_armed cs2_counts 0
scoreboard players set #reason cs2_counts 3
scoreboard players set #winner cs2_counts 1

execute at @e[type=marker,tag=cs2_bomb,limit=1] run particle minecraft:explosion_emitter ~ ~ ~ 1 1 1 0 1
execute at @e[type=marker,tag=cs2_bomb,limit=1] run particle minecraft:explosion ~ ~ ~ 3 3 3 0 10
execute at @e[type=marker,tag=cs2_bomb,limit=1] as @a[distance=..20,gamemode=!spectator] run damage @s 1000 minecraft:explosion
execute at @e[type=marker,tag=cs2_bomb,limit=1] run kill @e[type=item,distance=..20]

kill @e[tag=cs2_bomb]
kill @e[type=item_display,tag=cs2_c4_display]
kill @e[type=armor_stand,tag=cs2_c4_hit]
function cs2:round/end_t
