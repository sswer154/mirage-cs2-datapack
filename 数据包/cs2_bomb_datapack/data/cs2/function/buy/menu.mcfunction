# 准备阶段装备选择菜单：每名玩家单独收到自己的选项
# 第 1 回合固定手枪局，不显示选项。
execute if score #round cs2_round matches 1 run tellraw @a[team=cs2_t] {"text":"第 1 回合：手枪局，固定 ECO 装备（手枪 + 无甲）","color":"gold"}
execute if score #round cs2_round matches 1 run tellraw @a[team=cs2_ct] {"text":"第 1 回合：手枪局，固定 ECO 装备（手枪 + 无甲）","color":"gold"}
execute if score #round cs2_round matches 1 run return 0

execute as @a[team=cs2_t] run function cs2:buy/menu_player
execute as @a[team=cs2_ct] run function cs2:buy/menu_player