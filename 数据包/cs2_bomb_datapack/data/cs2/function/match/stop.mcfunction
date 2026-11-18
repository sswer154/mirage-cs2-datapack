# 停止比赛：仅让参赛者回大厅，不影响无关人员
gamerule keepInventory true
function cs2:match/reset
kill @e[tag=cs2_bomb]
kill @e[type=item_display,tag=cs2_c4_display]
kill @e[type=armor_stand,tag=cs2_c4_hit]
kill @e[type=item,tag=cs2_util_projectile]
kill @e[type=marker,tag=cs2_smoke]
kill @e[type=marker,tag=cs2_fire]
execute as @e[type=item] if data entity @s Item.components."minecraft:custom_data".cs2_bomb run kill @s
function cs2:pvp/on

# 先向参赛者播报，再把他们转回大厅；无关人员保持原状态
title @a[scores={cs2_inmatch=1,cs2_hud=1}] title {"text":"比赛已停止","color":"yellow"}
tellraw @a[scores={cs2_inmatch=1}] {"text":"比赛已停止，参赛者已离开队伍。","color":"yellow"}
playsound minecraft:block.note_block.bass master @a[scores={cs2_inmatch=1}]
execute as @a[scores={cs2_inmatch=1}] run function cs2:join/to_lobby
