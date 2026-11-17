# 拾取拆弹器：CT 获得持有分数；T/观战者拾取会被清除；已有拆弹器时不重复计数
advancement revoke @s only cs2:kit/pickup

# 非 CT 不能拿拆弹器
execute unless score @s cs2_team matches 2 run clear @s minecraft:shears[minecraft:custom_data={cs2_kit:1b}] 1
execute unless score @s cs2_team matches 2 run return 0

# 只有比赛进行/下包阶段允许持有
execute unless score #state cs2_state matches 3 run execute unless score #state cs2_state matches 4 run clear @s minecraft:shears[minecraft:custom_data={cs2_kit:1b}] 1
execute unless score #state cs2_state matches 3 run execute unless score #state cs2_state matches 4 run return 0

# 已经有拆弹器分数时不重复处理（避免回合开始时 give 触发 advancement 把钳子清掉）
execute if score @s cs2_kit matches 1 run return 0

scoreboard players set @s cs2_kit 1
title @s actionbar {"text":"已拾取拆弹器","color":"aqua"}
