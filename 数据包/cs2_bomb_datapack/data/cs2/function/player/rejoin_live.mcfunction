# 比赛进行中重连：存活者保留背包/效果并回到生存；阵亡/非参赛者才转观战
# 仍是本回合有效携带者才保留背包里的 C4。重连不能改变全局炸弹状态。
execute unless score #bomb cs2_bomb_state matches 1 run scoreboard players reset @s cs2_carrier
execute unless score @s cs2_alive matches 1 run scoreboard players reset @s cs2_carrier
execute unless entity @s[team=cs2_t,scores={cs2_inmatch=1,cs2_carrier=1}] run clear @s minecraft:nautilus_shell[minecraft:custom_data={cs2_bomb:1b}]
execute if score @s cs2_inmatch matches 1 run function cs2:util/night_vision
execute if score @s cs2_inmatch matches 1 run function cs2:util/freeze_unlock
execute if score @s cs2_inmatch matches 1 run cs2shop close
clear @s minecraft:diamond[minecraft:custom_data~{cs2_shop_item:1b}]
clear @s minecraft:stick[minecraft:custom_data~{cs2_shop_item:1b}]
execute if score #solo cs2_counts matches 0 run clear @s minecraft:emerald[minecraft:custom_data~{cs2_shop_item:1b}]
execute unless score @s cs2_inmatch matches 1 run function cs2:player/make_spec
execute unless score @s cs2_inmatch matches 1 run return 0
execute unless score @s cs2_team matches 1..2 run function cs2:player/make_spec
execute unless score @s cs2_team matches 1..2 run return 0
execute unless score @s cs2_alive matches 1 run function cs2:player/make_spec
execute unless score @s cs2_alive matches 1 if score #solo cs2_counts matches 0 run cs2fairplay watch
execute unless score @s cs2_alive matches 1 run return 0

function cs2:player/restore_health_if_due
gamemode survival @s
function cs2:util/speed
function cs2:player/tag
tellraw @s {"text":"已重新连接，比赛继续。","color":"green"}
