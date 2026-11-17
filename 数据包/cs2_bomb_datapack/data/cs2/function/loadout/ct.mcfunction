# ===== CT 配装分派（按每名玩家自己的 cs2_buy） =====
execute if score @s cs2_buy matches 1 run function cs2:loadout/ct/eco
execute if score @s cs2_buy matches 2 run function cs2:loadout/ct/force
execute if score @s cs2_buy matches 3 run function cs2:loadout/ct/rifle
execute if score @s cs2_buy matches 4 run function cs2:loadout/ct/sniper