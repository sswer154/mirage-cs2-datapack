# 拾取后归一：身上超过一把 C4 就清空重发一把
execute store result score #c4 cs2_counts run clear @s minecraft:nautilus_shell[minecraft:custom_data={cs2_bomb:1b}] 0
execute if score #c4 cs2_counts matches 2.. run function cs2:loadout/bomb_item
