# 重连/中途加入
# 升级清理：旧版商店木棍已废弃，重连时移除
clear @s minecraft:stick[minecraft:custom_data~{cs2_shop_item:1b}]
# 非参赛者：不自动传送、不改模式、不清背包；顺手清掉残留队伍
execute if score @s cs2_inmatch matches 0 if entity @s[team=cs2_t] run team leave @s
execute if score @s cs2_inmatch matches 0 if entity @s[team=cs2_ct] run team leave @s
execute if score @s cs2_inmatch matches 0 run return 0
execute if score #state cs2_state matches 1 run function cs2:player/rejoin_intermission
execute if score #state cs2_state matches 2 run function cs2:player/rejoin_freeze
execute if score #state cs2_state matches 3 run function cs2:player/rejoin_live
execute if score #state cs2_state matches 4 run function cs2:player/rejoin_live
execute if score #state cs2_state matches 5 run function cs2:player/rejoin_intermission
execute if score #state cs2_state matches 6 run function cs2:player/wait_lobby
execute if score #state cs2_state matches 7 run function cs2:player/make_spec
