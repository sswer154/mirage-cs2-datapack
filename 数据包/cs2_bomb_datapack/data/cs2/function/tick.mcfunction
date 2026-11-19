# 主循环：每 tick 执行
scoreboard players add #tick cs2_const 1

# 先处理上一 tick 标记的死亡播报/击杀标签过期；死亡检测在本 tick 稍后发生，
# 这样击杀 advancement 的标签一定已经写入，避免把击杀死亡误报成环境死亡。

# 实验性投掷物：烟雾弹 / 高爆手雷
# 死亡掉落物品规则：每 5 tick 扫描一次，避免每 tick 全实体查询
scoreboard players add #item_tick cs2_counts 1
execute if score #item_tick cs2_counts matches 5.. run function cs2:util/item_tick
execute if score #item_tick cs2_counts matches 5.. run scoreboard players set #item_tick cs2_counts 0
# 准备阶段持续维持冻结：医疗用品会清掉 slowness，这里每 tick 补回（跳跃锁走属性，不受影响）
execute if score #state cs2_state matches 2 as @a[scores={cs2_inmatch=1}] run function cs2:util/freeze_lock
# 准备阶段每 tick 幂等兜底：商店钻石 + 默认手枪，卡顿/重连后自动补发
execute if score #state cs2_state matches 2 as @a[scores={cs2_inmatch=1}] run function cs2:shop/ensure_ready

# 新玩家初始化
execute as @a unless score @s cs2_init matches 1 run function cs2:player/init
execute as @a[scores={cs2_init=1}] run function cs2:player/tick

# /trigger 入队/离队
execute as @a[scores={cs2_join_t=1..}] run function cs2:join/t
execute as @a[scores={cs2_join_ct=1..}] run function cs2:join/ct
execute as @a[scores={cs2_join_cast=1..}] run function cs2:join/cast
execute as @a[scores={cs2_leave=1..}] run function cs2:join/leave
execute as @a[scores={cs2_cancel_queue=1..}] run function cs2:join/cancel_queue
execute as @a[scores={cs2_hud_toggle=1..}] run function cs2:hud/trigger_toggle
execute as @a[scores={cs2_stats=1..}] run function cs2:stats/trigger
execute as @a[scores={cs2_panel=1..}] run function cs2:panel/trigger
execute as @a[scores={cs2_pause=1..}] run function cs2:panel/pause_request
execute as @a[scores={cs2_vote_restart=1..}] run function cs2:panel/vote_restart
execute as @a[scores={cs2_vote_end=1..}] run function cs2:panel/vote_end
execute as @a[scores={cs2_practice_leave=1..}] run function cs2:practice/leave
execute as @a[scores={cs2_train_hold=1..}] run function cs2:practice/hold_timer
execute as @a[scores={cs2_train_spawn=1..}] run function cs2:practice/spawn_enemy
execute as @a[scores={cs2_match_pause=1..}] run function cs2:panel/match_pause
execute as @a[scores={cs2_match_resume=1..}] run function cs2:panel/match_resume

# 兜底：比赛进行中但场上一个参赛者都没有，持续 10 秒就自动结束
execute if score #state cs2_state matches 1..6 unless entity @a[scores={cs2_inmatch=1}] run scoreboard players add #empty cs2_empty 1
execute if score #state cs2_state matches 1..6 if entity @a[scores={cs2_inmatch=1}] run scoreboard players set #empty cs2_empty 0
execute if score #state cs2_state matches 1..6 if score #empty cs2_empty matches 200.. run function cs2:match/end
execute unless score #state cs2_state matches 1..6 run scoreboard players set #empty cs2_empty 0

# 战术暂停：停住回合计时/炸弹倒计时/胜负检查，只更新暂停倒计时
execute if score #paused cs2_counts matches 1 run function cs2:panel/pause_tick
execute if score #paused cs2_counts matches 1 run return 0

# 在处理离队和暂停后，以当前在线参赛者重新核定正在进行的投票。
execute if score #solo cs2_counts matches 0 if score #state cs2_state matches 3..4 if entity @a[scores={cs2_inmatch=1,cs2_voted_restart=1}] run function cs2:panel/check_votes
execute if score #solo cs2_counts matches 0 if score #state cs2_state matches 2..4 if entity @a[scores={cs2_inmatch=1,cs2_voted_end=1}] run function cs2:panel/check_votes

# 双方在准备阶段组齐时，用干净的正式经济与装备重新开始购买阶段。
execute if score #state cs2_state matches 2 if score #solo cs2_counts matches 1..2 if entity @a[team=cs2_t,scores={cs2_inmatch=1}] if entity @a[team=cs2_ct,scores={cs2_inmatch=1}] run return run function cs2:practice/to_formal
# 状态计时
execute if score #state cs2_state matches 1 run function cs2:round/warmup_tick
execute if score #state cs2_state matches 2 run function cs2:round/freeze_tick
execute if score #state cs2_state matches 3 run function cs2:round/live_tick
execute if score #state cs2_state matches 4 run function cs2:bomb/tick
execute if score #state cs2_state matches 5 run function cs2:round/end_tick
execute if score #state cs2_state matches 6 run function cs2:round/break_tick

# 胜负检查
execute if score #state cs2_state matches 3 run function cs2:round/check_live
execute if score #state cs2_state matches 3 run function cs2:round/check_time_live
execute if score #state cs2_state matches 4 run function cs2:round/check_planted

# 炸弹掉落/拾取检测
execute if score #state cs2_state matches 3 run function cs2:bomb/drop_check
# 拆弹器掉落/拾取检测

# HUD
function cs2:hud/tick

# 测试商店按钮的条件检查始终通过；训练商店只会在单人测试局由 give_test_shop 发放
scoreboard players set #testshop cs2_testshop 1
