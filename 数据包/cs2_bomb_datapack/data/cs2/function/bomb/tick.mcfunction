scoreboard players remove #bomb_timer cs2_bomb_timer 1
execute if score #bomb_timer cs2_bomb_timer matches ..0 run function cs2:bomb/explode
execute if score #state cs2_state matches 4 run function cs2:bomb/defuse_tick
execute if score #bomb_timer cs2_bomb_timer matches ..200 if score #state cs2_state matches 4 run function cs2:bomb/blink
execute if score #state cs2_state matches 4 run function cs2:bomb/bossbar
