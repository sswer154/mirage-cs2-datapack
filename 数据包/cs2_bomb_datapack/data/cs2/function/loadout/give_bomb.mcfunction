# 随机一名 T 拿到 C4
execute if score #bomb cs2_bomb_state matches 0 as @r[team=cs2_t,scores={cs2_inmatch=1,cs2_alive=1}] run function cs2:loadout/bomb_item
