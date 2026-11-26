scoreboard players set #paused cs2_counts 0
scoreboard players set #pause_kind cs2_counts 0
scoreboard players set #pause_timer cs2_counts 0
scoreboard players set #pause_state cs2_counts 0
scoreboard players set #mpause_t cs2_counts 0
scoreboard players set #mpause_c cs2_counts 0
team modify cs2_t friendlyFire true
team modify cs2_ct friendlyFire true
effect clear @a[scores={cs2_inmatch=1}] minecraft:slowness
effect clear @a[scores={cs2_inmatch=1}] minecraft:resistance
execute as @a[scores={cs2_inmatch=1}] run function cs2:util/speed
gamemode adventure @a[scores={cs2_inmatch=1,cs2_alive=1}]
effect give @a[scores={cs2_inmatch=1}] minecraft:slowness 30 255 true
effect give @a[scores={cs2_inmatch=1}] minecraft:resistance 30 255 true
bossbar set cs2:round visible true
execute store result bossbar cs2:round max run scoreboard players get #timer cs2_timer
execute store result bossbar cs2:round value run scoreboard players get #timer cs2_timer
title @a[scores={cs2_inmatch=1}] times 10 40 10
title @a[scores={cs2_inmatch=1,cs2_hud=1}] title {"text":"暂停结束","color":"green","bold":true}
title @a[scores={cs2_inmatch=1,cs2_hud=1}] subtitle {"text":"继续准备","color":"gray"}
playsound minecraft:block.note_block.pling master @a[scores={cs2_inmatch=1}]
