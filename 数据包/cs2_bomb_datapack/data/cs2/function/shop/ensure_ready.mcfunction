# 准备阶段每 tick 幂等兜底：卡顿跨阶段、重连后都从这里自动补齐
execute unless score #state cs2_state matches 2 run return 0
execute unless score @s cs2_inmatch matches 1 run return 0

execute if score @s cs2_team matches 1 run function cs2:shop/ensure_default_t
execute if score @s cs2_team matches 2 run function cs2:shop/ensure_default_ct
function cs2:util/give_shop_item
