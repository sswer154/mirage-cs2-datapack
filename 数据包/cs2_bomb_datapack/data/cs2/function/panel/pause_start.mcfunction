scoreboard players set #paused cs2_counts 1
scoreboard players set #pause_kind cs2_counts 1
scoreboard players operation #pause_state cs2_counts = #state cs2_state
scoreboard players set #pause_timer cs2_counts 600
team modify cs2_t friendlyFire false
team modify cs2_ct friendlyFire false
gamemode adventure @a[scores={cs2_inmatch=1}]
effect give @a[scores={cs2_inmatch=1}] minecraft:slowness 30 255 true
effect give @a[scores={cs2_inmatch=1}] minecraft:resistance 30 255 true
bossbar set cs2:round visible true
bossbar set cs2:round max 600
bossbar set cs2:round value 600
title @a[scores={cs2_inmatch=1}] times 10 60 10
title @a[scores={cs2_inmatch=1,cs2_hud=1}] title {"text":"战术暂停","color":"aqua","bold":true}
title @a[scores={cs2_inmatch=1,cs2_hud=1}] subtitle {"text":"30 秒 · 双方停止行动","color":"gray"}
playsound minecraft:block.note_block.pling master @a[scores={cs2_inmatch=1}]
tellraw @a[scores={cs2_inmatch=1}] [{"text":"战术暂停：","color":"aqua"},{"text":"T ","color":"red"},{"score":{"name":"#pause_t","objective":"cs2_counts"}},{"text":"/3  CT ","color":"blue"},{"score":{"name":"#pause_c","objective":"cs2_counts"}},{"text":"/3","color":"blue"}]
