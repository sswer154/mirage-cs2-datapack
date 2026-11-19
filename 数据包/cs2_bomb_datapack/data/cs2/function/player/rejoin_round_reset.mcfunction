function cs2:player/round_reset
execute if score @s cs2_needs_default matches 1 if score @s cs2_team matches 1 run function cs2:shop/default_t
execute if score @s cs2_needs_default matches 1 if score @s cs2_team matches 2 run function cs2:shop/default_ct
scoreboard players set @s cs2_needs_default 0
function cs2:shop/snapshot_carry
