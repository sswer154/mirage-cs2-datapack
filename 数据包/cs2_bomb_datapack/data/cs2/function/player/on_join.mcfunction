# 重连/中途加入
# 等候开赛的玩家保留阵营和选边时保存的背包。
execute unless score #state cs2_state matches 1..6 if score @s cs2_ready_team matches 1 if entity @s[team=cs2_t] run return 0
execute unless score #state cs2_state matches 1..6 if score @s cs2_ready_team matches 1 if entity @s[team=cs2_ct] run return 0
# 错过开场的玩家可在正常准备阶段补进场。开打后继续等下一次开场。
execute if score #state cs2_state matches 2 unless score #round_replayed cs2_counts matches 1 if score @s cs2_queued_team matches 1..2 unless score @s cs2_queue_round = #checkpoint cs2_health_round run function cs2:join/apply_queue
execute if score #state cs2_state matches 1..6 unless score @s cs2_inmatch matches 1 if score @s cs2_queued_team matches 1..2 run return 0
# 赛后离线玩家先按退队流程清理状态，再返还背包。
execute unless score @s cs2_inmatch matches 1 if function cs2:player/has_saved_inventory run return run function cs2:join/to_lobby
# 升级清理：旧版商店木棍已废弃，参赛者重连时移除。
execute if score @s cs2_inmatch matches 1 run clear @s minecraft:stick[minecraft:custom_data~{cs2_shop_item:1b}]
# 非参赛者：不自动传送、不改模式、不清背包；顺手清掉残留队伍
execute if score @s cs2_inmatch matches 0 if entity @s[team=cs2_t] run team leave @s
execute if score @s cs2_inmatch matches 0 if entity @s[team=cs2_ct] run team leave @s
execute if score @s cs2_inmatch matches 0 run return 0
# 先补阵营和经济，再按当前阵营初始化回合状态。
execute if score #state cs2_state matches 1..6 if score @s cs2_inmatch matches 1 if score @s cs2_team matches 1..2 run function cs2:join/sync_side
execute if score #state cs2_state matches 1..6 if score @s cs2_inmatch matches 1 if score @s cs2_team matches 1..2 if score #transition cs2_formal_epoch matches 1.. unless score @s cs2_formal_epoch = #transition cs2_formal_epoch run function cs2:practice/formal_player
execute if score #state cs2_state matches 1 run function cs2:player/rejoin_intermission
# 补做错过的回合初始化，再按当前回合状态处理重连。
execute if score #state cs2_state matches 2..4 if score @s cs2_inmatch matches 1 if score @s cs2_team matches 1..2 if score #epoch cs2_health_round matches 1.. unless score @s cs2_health_round = #epoch cs2_health_round run function cs2:player/rejoin_round_reset
execute if score #state cs2_state matches 2 run function cs2:player/rejoin_freeze
execute if score #state cs2_state matches 3 run function cs2:player/rejoin_live
execute if score #state cs2_state matches 4 run function cs2:player/rejoin_live
execute if score #state cs2_state matches 5 run function cs2:player/rejoin_intermission
execute if score #state cs2_state matches 6 run function cs2:player/wait_lobby
execute if score #state cs2_state matches 7 run function cs2:player/make_spec
# 比赛已停止/未开始（state 0）时，残留的参赛者也要清理锁跳/减速
execute if score #state cs2_state matches 0 if score @s cs2_inmatch matches 1 run function cs2:player/make_spec
# 当阵营、装备和经济补初始化完成后，结清此前离线的回合奖励。
execute if score #state cs2_state matches 1..6 if score @s cs2_inmatch matches 1 run cs2econ claim
# 拆弹器改为纯标记：清理旧版遗留物品
execute as @s run clear @s minecraft:shears[minecraft:custom_data={cs2_kit:1b}]
