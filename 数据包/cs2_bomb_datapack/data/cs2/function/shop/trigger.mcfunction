# 现行战术菜单和历史聊天按钮共用入口，购买操作由商店 GUI 处理。
execute if score @s cs2_shop matches 99 run return run function cs2:shop/close_trigger
scoreboard players reset @s cs2_shop
scoreboard players enable @s cs2_shop
execute unless score #state cs2_state matches 2 run return run tellraw @s {"text":"商店仅在准备阶段开放。","color":"red"}
execute unless entity @s[gamemode=!spectator,scores={cs2_inmatch=1,cs2_alive=1,cs2_team=1..2}] run return run tellraw @s {"text":"只有存活的参赛者可以打开商店。","color":"red"}
cs2shop open cs2:main
