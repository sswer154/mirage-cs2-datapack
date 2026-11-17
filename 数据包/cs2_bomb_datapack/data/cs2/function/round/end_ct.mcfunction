execute unless score #state cs2_state matches 3 run execute unless score #state cs2_state matches 4 run return 0
scoreboard players add #ct cs2_score_ct 1
scoreboard players set #winner cs2_counts 2
function cs2:round/end
