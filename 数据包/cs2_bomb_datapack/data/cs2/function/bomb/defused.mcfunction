execute unless score #state cs2_state matches 4 run return 0
scoreboard players set #reason cs2_counts 4
scoreboard players set #winner cs2_counts 2
kill @e[tag=cs2_bomb]
kill @e[type=item_display,tag=cs2_c4_display]
playsound minecraft:block.beacon.deactivate master @a
function cs2:round/end_ct
