execute unless score #state cs2_state matches 3 run return 0
scoreboard players set #bomb cs2_bomb_state 0
scoreboard players set #drop_timer cs2_counts 0
function cs2:loadout/give_bomb
execute if score #bomb cs2_bomb_state matches 0 run title @a[scores={cs2_hud=1}] actionbar {"text":"C4 无法重新发放：没有存活的 T","color":"red"}
