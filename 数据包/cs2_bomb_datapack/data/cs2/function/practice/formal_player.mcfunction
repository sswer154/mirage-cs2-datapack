cs2shop close
function cs2:player/round_reset_clear
scoreboard players set @s cs2_money 800
scoreboard players set @s cs2_alive 0
scoreboard players set @s cs2_kills 0
scoreboard players reset @s cs2_deaths
scoreboard players set @s cs2_prev_deaths 0
scoreboard players set @s cs2_death_pending 0
scoreboard players operation @s cs2_formal_epoch = #transition cs2_formal_epoch
