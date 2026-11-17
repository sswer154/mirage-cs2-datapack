# 玩家选择狙击（狙击枪 + 铁甲）
execute unless score #state cs2_state matches 2 run tellraw @s {"text":"只能在准备阶段选择装备。","color":"red"}
execute unless score #state cs2_state matches 2 run return 0
execute unless score @s cs2_allow_sniper matches 1 run tellraw @s {"text":"本回合不允许选择狙击。","color":"red"}
execute unless score @s cs2_allow_sniper matches 1 run return 0
execute unless score @s cs2_team matches 1..2 run tellraw @s {"text":"观战者不能选择装备。","color":"red"}
execute unless score @s cs2_team matches 1..2 run return 0
scoreboard players set @s cs2_choice 4
execute if score @s cs2_team matches 1 run tellraw @a[team=cs2_t] [{"text":"[装备] ","color":"gray"},{"selector":"@s"},{"text":" → 狙击（狙击枪 + 铁甲）","color":"light_purple"}]
execute if score @s cs2_team matches 2 run tellraw @a[team=cs2_ct] [{"text":"[装备] ","color":"gray"},{"selector":"@s"},{"text":" → 狙击（狙击枪 + 铁甲）","color":"light_purple"}]
playsound minecraft:ui.button.click master @s