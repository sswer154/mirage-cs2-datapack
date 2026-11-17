# 比赛暂停：无时限，保留准备阶段状态，直到任意参赛者解除
scoreboard players set #paused cs2_counts 1
scoreboard players set #pause_kind cs2_counts 2
scoreboard players set #pause_timer cs2_counts 0
scoreboard players operation #pause_state cs2_counts = #state cs2_state
scoreboard players set #mpause_t cs2_counts 0
scoreboard players set #mpause_c cs2_counts 0
team modify cs2_t friendlyFire false
team modify cs2_ct friendlyFire false
gamemode adventure @a[scores={cs2_inmatch=1}]
effect give @a[scores={cs2_inmatch=1}] minecraft:slowness infinite 255 true
effect give @a[scores={cs2_inmatch=1}] minecraft:resistance infinite 255 true
bossbar set cs2:round visible false
title @a[scores={cs2_inmatch=1}] times 10 70 10
title @a[scores={cs2_inmatch=1,cs2_hud=1}] title {"text":"比赛暂停","color":"aqua","bold":true}
title @a[scores={cs2_inmatch=1,cs2_hud=1}] subtitle {"text":"双方同意 · 无时间限制 · 任意参赛者可解除","color":"gray"}
playsound minecraft:block.note_block.pling master @a[scores={cs2_inmatch=1}]
tellraw @a[scores={cs2_inmatch=1}] [{"text":"比赛暂停已生效。","color":"aqua","bold":true},{"text":" 打开战术菜单可随时解除。","color":"gray"}]
