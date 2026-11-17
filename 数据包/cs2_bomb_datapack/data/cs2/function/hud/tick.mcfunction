# 每 5 tick 刷新 HUD
scoreboard players add #hud_tick cs2_const 1
execute if score #hud_tick cs2_const matches 5.. run function cs2:hud/update
execute if score #hud_tick cs2_const matches 5.. run scoreboard players set #hud_tick cs2_const 0
