# 单个玩家的装备菜单；只有本人能看到选项
tellraw @s {"text":"──────── 装备选择 ────────","color":"gold"}
tellraw @s {"text":"下面的选项只有你能看到；选择后会通知本队队友。","color":"gray"}
execute if score @s cs2_allow_eco matches 1 run tellraw @s [{"text":"[ECO] 手枪 + 无甲","color":"green","clickEvent":{"action":"run_command","value":"/trigger cs2_buy_eco"}}]
execute if score @s cs2_allow_force matches 1 run tellraw @s [{"text":"[强起] 冲锋枪 + 无甲","color":"yellow","clickEvent":{"action":"run_command","value":"/trigger cs2_buy_force"}}]
execute if score @s cs2_allow_rifle matches 1 run tellraw @s [{"text":"[长枪] 步枪 + 铁甲","color":"aqua","clickEvent":{"action":"run_command","value":"/trigger cs2_buy_rifle"}}]
execute if score @s cs2_allow_sniper matches 1 run tellraw @s [{"text":"[狙击] 狙击枪 + 铁甲","color":"light_purple","clickEvent":{"action":"run_command","value":"/trigger cs2_buy_sniper"}}]
execute unless score @s cs2_allow_eco matches 1 unless score @s cs2_allow_force matches 1 unless score @s cs2_allow_rifle matches 1 unless score @s cs2_allow_sniper matches 1 run tellraw @s {"text":"本回合没有可选装备，将使用默认装备。","color":"gray"}
tellraw @s {"text":"未选择时自动使用默认装备。","color":"gray"}