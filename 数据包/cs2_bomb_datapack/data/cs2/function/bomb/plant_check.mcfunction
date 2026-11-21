# C4 安放读条完成后由模组调用
execute unless score @s cs2_team matches 1 run return 0
execute unless score #state cs2_state matches 3 run return 0
execute unless score @s cs2_inmatch matches 1 run return 0
execute unless score @s cs2_alive matches 1 run return 0
execute unless items entity @s weapon.mainhand minecraft:nautilus_shell[minecraft:custom_data~{cs2_bomb:1b}] run return 0
execute unless score @s cs2_carrier matches 1 run return 0

execute at @s if entity @e[type=marker,tag=cs2_site_a,distance=..7] run function cs2:bomb/plant_a
execute if score #state cs2_state matches 4 run return 0
execute at @s if entity @e[type=marker,tag=cs2_site_b,distance=..7] run function cs2:bomb/plant_b
execute if score #state cs2_state matches 4 run return 0
function cs2:bomb/return
