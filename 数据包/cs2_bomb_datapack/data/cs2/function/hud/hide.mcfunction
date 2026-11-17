# 只关闭该玩家自己的动作栏/回合标题；不影响队友
scoreboard players set @s cs2_hud 0
title @s actionbar ""
tellraw @s {"text":"你的 HUD 已隐藏。用 /trigger cs2_hud_toggle 重新显示。","color":"gray"}