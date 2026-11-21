# 阵亡：保留 cs2_team（下一回合自动回队），但本回合变成观战
scoreboard players set @s cs2_alive 0
# 标记下一回合需要补发默认装备，避免死亡发生在 freeze/回合结束等阶段时漏发
scoreboard players set @s cs2_needs_default 1
execute if score @s cs2_kit matches 1 run function cs2:kit/on_death
scoreboard players set @s cs2_carrier 0
scoreboard players set @s cs2_defuse 0
gamemode spectator @s
effect clear @s
execute if score @s cs2_inmatch matches 1 run function cs2:util/night_vision
function cs2:player/spectate
title @s title {"text":"你已阵亡","color":"red"}
title @s subtitle {"text":"等待下一回合","color":"gray"}
playsound minecraft:entity.player.death master @s
