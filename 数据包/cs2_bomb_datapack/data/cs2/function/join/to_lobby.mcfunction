execute if entity @s[team=cs2_t] run team leave @s
execute if entity @s[team=cs2_ct] run team leave @s
execute if entity @s[team=cs2_cast] run team leave @s
scoreboard players set @s cs2_team 0
scoreboard players set @s cs2_alive 0
scoreboard players set @s cs2_carrier 0
scoreboard players set @s cs2_kit 0
scoreboard players set @s cs2_inmatch 0
scoreboard players reset @s cs2_voted_restart
scoreboard players reset @s cs2_voted_end
scoreboard players reset @s cs2_ready_team
function cs2:join/clear_queue
scoreboard players reset @s cs2_pending_pay
scoreboard players set @s cs2_needs_default 0
scoreboard players set @s cs2_was_alive 0
function cs2:player/tag
clear @s minecraft:nautilus_shell[minecraft:custom_data={cs2_bomb:1b}]
clear @s minecraft:shears[minecraft:custom_data={cs2_kit:1b}]
clear @s minecraft:diamond[minecraft:custom_data~{cs2_shop_item:1b}]
clear @s minecraft:stick[minecraft:custom_data~{cs2_shop_item:1b}]
effect clear @s minecraft:speed
effect clear @s minecraft:night_vision
effect clear @s minecraft:invisibility
effect clear @s minecraft:slowness
effect clear @s minecraft:resistance
function cs2:util/jump_unlock
gamemode adventure @s
cs2inventory restore
