execute unless score #state cs2_state matches 4 run return 0
scoreboard players set #reason cs2_counts 4
scoreboard players set #winner cs2_counts 2
kill @e[tag=cs2_bomb]
kill @e[type=item_display,tag=cs2_c4_display]
kill @e[type=armor_stand,tag=cs2_c4_hit]
function cs2:util/feed_target
# 拆除成功提示音：只给参赛者与解说
scoreboard players set #c4_sfx cs2_sfx 2
tag @s add cs2_last_defuser
function cs2:round/end_ct
