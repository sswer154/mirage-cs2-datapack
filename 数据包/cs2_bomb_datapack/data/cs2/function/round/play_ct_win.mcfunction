execute unless score #state cs2_state matches 5 run return 0
execute as @a[predicate=cs2:match_audio] at @s run playsound cs2:round.ct_win player @s
execute as @a[team=cs2_cast] unless predicate cs2:match_audio at @s run playsound cs2:round.ct_win player @s
