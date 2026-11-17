# 回合开始时记录玩家身上已有的道具数量，购买上限按“带入 + 本回合已买”计算
function cs2:shop/count_util
scoreboard players operation @s cs2_carry_start = #util_total cs2_counts
