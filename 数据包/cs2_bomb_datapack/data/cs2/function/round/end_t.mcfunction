execute unless score #state cs2_state matches 3 run execute unless score #state cs2_state matches 4 run return 0
scoreboard players add #t cs2_score_t 1
scoreboard players set #winner cs2_counts 1
function cs2:round/end
