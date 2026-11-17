# consume_item 触发器：吃完 C4 时调用
advancement revoke @s only cs2:bomb/plant
execute unless score @s cs2_team matches 1 run return 0
execute unless score #state cs2_state matches 3 run return 0
execute unless score @s cs2_carrier matches 1 run function cs2:bomb/return
execute unless score @s cs2_carrier matches 1 run return 0

execute at @s if entity @e[type=marker,tag=cs2_site_a,distance=..10] run function cs2:bomb/plant_a
execute if score #state cs2_state matches 4 run return 0
execute at @s if entity @e[type=marker,tag=cs2_site_b,distance=..10] run function cs2:bomb/plant_b
execute if score #state cs2_state matches 4 run return 0
function cs2:bomb/return
