# 切换每名玩家自己的 HUD（动作栏/回合标题）；侧边栏为原版全局显示
scoreboard players operation #hud_old cs2_const = @s cs2_hud
execute if score #hud_old cs2_const matches 1 run function cs2:hud/hide
execute if score #hud_old cs2_const matches 0 run function cs2:hud/show