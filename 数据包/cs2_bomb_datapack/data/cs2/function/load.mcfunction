# 载入入口
# 说明：cs2_const 在每次 reload 时会报一次“already exists”，这是正常的；
# 它只是为了在首次安装时创建一次基础内容。
scoreboard objectives add cs2_const dummy
scoreboard objectives add cs2_match_scaled dummy
scoreboard objectives add cs2_health_round dummy
scoreboard objectives add cs2_side_epoch dummy
scoreboard objectives add cs2_formal_epoch dummy
scoreboard objectives add cs2_empty dummy
scoreboard objectives add cs2_pending_pay dummy
scoreboard objectives add cs2_ready_team dummy
scoreboard objectives add cs2_queued_team dummy
scoreboard objectives add cs2_queue_round dummy
scoreboard objectives add cs2_cancel_queue trigger
scoreboard objectives add cs2_voted_restart dummy
scoreboard objectives add cs2_voted_end dummy
execute unless score #purchase_ready cs2_const matches 1 run scoreboard objectives add cs2_item_count dummy
execute unless score #purchase_ready cs2_const matches 1 run scoreboard objectives add cs2_carry_start dummy
execute unless score #purchase_ready cs2_const matches 1 run scoreboard objectives add cs2_util_he_fast dummy
execute unless score #purchase_ready cs2_const matches 1 run scoreboard objectives add cs2_util_impact dummy
execute unless score #purchase_ready cs2_const matches 1 run scoreboard objectives add cs2_util_surgery dummy
execute unless score #purchase_ready cs2_const matches 1 run scoreboard objectives add cs2_util_golden dummy
execute unless score #purchase_ready cs2_const matches 1 run scoreboard players set #purchase_ready cs2_const 1
scoreboard players add #swap cs2_side_epoch 0
scoreboard players add #halftime cs2_side_epoch 0
scoreboard players set #side_mod cs2_side_epoch 2
# 新版本新增的触发项；老存档首次 reload 时补建，之后用 #hud_ready 跳过。
execute unless score #hud_ready cs2_const matches 1 run scoreboard objectives add cs2_hud_toggle trigger
execute unless score #hud_ready cs2_const matches 1 run scoreboard players set #hud_ready cs2_const 1

# 新经济系统 / 聊天栏商店：老存档首次 reload 时补建
execute unless score #shop_ready cs2_const matches 1 run scoreboard objectives add cs2_money dummy
execute unless score #shop_ready cs2_const matches 1 run scoreboard objectives add cs2_econ dummy
execute unless score #shop_ready cs2_const matches 1 run scoreboard objectives add cs2_primary dummy
execute unless score #shop_ready cs2_const matches 1 run scoreboard objectives add cs2_secondary dummy
execute unless score #shop_ready cs2_const matches 1 run scoreboard objectives add cs2_armor dummy
execute unless score #shop_ready cs2_const matches 1 run scoreboard objectives add cs2_planter dummy
execute unless score #shop_ready cs2_const matches 1 run scoreboard objectives add cs2_wclass dummy
execute unless score #shop_ready cs2_const matches 1 run scoreboard objectives add cs2_shop trigger
execute unless score #shop_ready cs2_const matches 1 run scoreboard players set #shop_ready cs2_const 1
scoreboard players set #5 cs2_const 5
scoreboard players set #20 cs2_const 20
scoreboard players set #100 cs2_const 100
scoreboard players set #50 cs2_const 50

# 每玩家独立经济/HUD 所需的附加计分项
execute unless score #perplayer_ready cs2_const matches 1 run scoreboard objectives add cs2_choice dummy
execute unless score #perplayer_ready cs2_const matches 1 run scoreboard objectives add cs2_allow_eco dummy
execute unless score #perplayer_ready cs2_const matches 1 run scoreboard objectives add cs2_allow_force dummy
execute unless score #perplayer_ready cs2_const matches 1 run scoreboard objectives add cs2_allow_rifle dummy
execute unless score #perplayer_ready cs2_const matches 1 run scoreboard objectives add cs2_allow_sniper dummy
execute unless score #perplayer_ready cs2_const matches 1 run scoreboard players set #perplayer_ready cs2_const 1

# 参赛标记 / 回合初始装备标记 / 上局存活标记
execute unless score #match_ready cs2_const matches 1 run scoreboard objectives add cs2_inmatch dummy
execute unless score #match_ready cs2_const matches 1 run scoreboard objectives add cs2_needs_default dummy
execute unless score #match_ready cs2_const matches 1 run scoreboard objectives add cs2_was_alive dummy
execute unless score #match_ready cs2_const matches 1 run scoreboard players set #match_ready cs2_const 1

# K/D 统计与击杀播报：老存档首次 reload 时补建
execute unless score #stats_ready cs2_const matches 1 run scoreboard objectives add cs2_kills dummy
execute unless score #stats_ready cs2_const matches 1 run scoreboard objectives add cs2_death_pending dummy
execute unless score #stats_ready cs2_const matches 1 run scoreboard objectives add cs2_killer_timer dummy
execute unless score #stats_ready cs2_const matches 1 run scoreboard objectives add cs2_stats trigger
execute unless score #stats_ready cs2_const matches 1 run scoreboard players set #stats_ready cs2_const 1

# 回合高光统计：每回合击杀/燃烧伤害/手雷伤害
execute unless score #highlight_ready cs2_const matches 1 run scoreboard objectives add cs2_round_kills dummy
execute unless score #highlight_ready cs2_const matches 1 run scoreboard objectives add cs2_fire_dmg dummy
execute unless score #highlight_ready cs2_const matches 1 run scoreboard objectives add cs2_grenade_dmg dummy
execute unless score #highlight_ready cs2_const matches 1 run scoreboard players set #highlight_ready cs2_const 1

# 实验性投掷物（烟雾弹 / 高爆手雷 / 闪光弹）
execute unless score #util_ready cs2_const matches 1 run scoreboard objectives add cs2_util_fuse dummy
execute unless score #util_ready cs2_const matches 1 run scoreboard objectives add cs2_util_duration dummy
execute unless score #util_ready cs2_const matches 1 run scoreboard players set #util_ready cs2_const 1

# 闪光弹计分项单独迁移：老存档的 #util_ready 可能已经是 1，不能再共用这个守卫
execute unless score #flash_ready cs2_const matches 1 run scoreboard objectives add cs2_flash_timer dummy
execute unless score #flash_ready cs2_const matches 1 run scoreboard objectives add cs2_flash_seen dummy
execute unless score #flash_ready cs2_const matches 1 run scoreboard objectives add cs2_flash_steps dummy
execute unless score #flash_ready cs2_const matches 1 run scoreboard objectives add cs2_flash_alpha dummy
execute unless score #flash_ready cs2_const matches 1 run scoreboard players set #flash_ready cs2_const 1

# 投掷物每回合限购 1 个：旧存档独立补建
execute unless score #utility_ready cs2_const matches 1 run scoreboard objectives add cs2_util_smoke dummy
execute unless score #utility_ready cs2_const matches 1 run scoreboard objectives add cs2_util_he dummy
execute unless score #utility_ready cs2_const matches 1 run scoreboard objectives add cs2_util_flash dummy
execute unless score #utility_ready cs2_const matches 1 run scoreboard objectives add cs2_util_fire dummy
execute unless score #utility_ready cs2_const matches 1 run scoreboard players set #utility_ready cs2_const 1
scoreboard players set #util_pulse cs2_const 0

# 训练商店条件：旧存档/合并包漏建 cs2_testshop objective 时补建
execute unless score #testshop_ready cs2_const matches 1 run scoreboard objectives add cs2_testshop dummy
execute unless score #testshop_ready cs2_const matches 1 run scoreboard players set #testshop cs2_testshop 0
execute unless score #testshop_ready cs2_const matches 1 run scoreboard players set #testshop_ready cs2_const 1

# 战术菜单/投票/练习退出：旧存档首次 reload 补建触发器
execute unless score #panel_ready cs2_const matches 1 run scoreboard objectives add cs2_panel trigger
execute unless score #panel_ready cs2_const matches 1 run scoreboard objectives add cs2_pause trigger
execute unless score #panel_ready cs2_const matches 1 run scoreboard objectives add cs2_vote_restart trigger
execute unless score #panel_ready cs2_const matches 1 run scoreboard objectives add cs2_vote_end trigger
execute unless score #panel_ready cs2_const matches 1 run scoreboard objectives add cs2_practice_leave trigger
execute unless score #panel_ready cs2_const matches 1 run scoreboard players set #panel_ready cs2_const 1

# 训练模式：暂停倒计时 / 召唤训练敌人（旧存档首次 reload 补建）
execute unless score #train_ready cs2_const matches 1 run scoreboard objectives add cs2_train_hold trigger
execute unless score #train_ready cs2_const matches 1 run scoreboard objectives add cs2_train_spawn trigger
execute unless score #train_ready cs2_const matches 1 run scoreboard players set #train_ready cs2_const 1

# 比赛暂停：投票发起 + 随时解除（旧存档补建）
execute unless score #mpause_ready cs2_const matches 1 run scoreboard objectives add cs2_match_pause trigger
execute unless score #mpause_ready cs2_const matches 1 run scoreboard objectives add cs2_match_resume trigger
execute unless score #mpause_ready cs2_const matches 1 run scoreboard players set #mpause_ready cs2_const 1

# 拆弹检测：用原版统计 sneak_time 的每 tick 增量判断是否潜行，避免依赖玩家 NBT 的 Sneaking 字段
execute unless score #sneak_ready cs2_const matches 1 run scoreboard objectives add cs2_sneak minecraft.custom:minecraft.sneak_time
execute unless score #sneak_ready cs2_const matches 1 run scoreboard objectives add cs2_sneak_prev dummy
execute unless score #sneak_ready cs2_const matches 1 run scoreboard objectives add cs2_sneak_delta dummy
execute unless score #sneak_ready cs2_const matches 1 run scoreboard players set #sneak_ready cs2_const 1
# 在线老玩家先对齐一次，避免首次增量把历史 sneak_time 全部算进去
execute as @a run scoreboard players operation @s cs2_sneak_prev = @s cs2_sneak

# 记录本次 reload 前是否已经加载过，用于一次性迁移旧版旁观队伍
scoreboard players operation #old_loaded cs2_const = #loaded cs2_const
execute unless score #loaded cs2_const matches 1 run function cs2:load/first
# 旧版本有 cs2_spec 旁观队伍；升级后只保留 T/CT，移除旧队伍
execute if score #old_loaded cs2_const matches 1 unless score #no_spec_ready cs2_const matches 1 run team remove cs2_spec
execute unless score #no_spec_ready cs2_const matches 1 run scoreboard players set #no_spec_ready cs2_const 1

# 解说队伍：旧存档首次 reload 时补建；新存档由 load/first 创建
execute if score #old_loaded cs2_const matches 1 unless score #cast_ready cs2_const matches 1 run team add cs2_cast
execute if score #old_loaded cs2_const matches 1 unless score #cast_ready cs2_const matches 1 run team modify cs2_cast color dark_aqua
execute if score #old_loaded cs2_const matches 1 unless score #cast_ready cs2_const matches 1 run team modify cs2_cast friendlyFire false
execute if score #old_loaded cs2_const matches 1 unless score #cast_ready cs2_const matches 1 run team modify cs2_cast seeFriendlyInvisibles true
execute if score #old_loaded cs2_const matches 1 unless score #cast_ready cs2_const matches 1 run team modify cs2_cast nametagVisibility hideForOtherTeams
execute if score #old_loaded cs2_const matches 1 unless score #cast_ready cs2_const matches 1 run team modify cs2_cast deathMessageVisibility hideForOtherTeams
execute if score #old_loaded cs2_const matches 1 unless score #cast_ready cs2_const matches 1 run team modify cs2_cast collisionRule never
execute if score #old_loaded cs2_const matches 1 unless score #cast_ready cs2_const matches 1 run team modify cs2_cast prefix {"text":"[解说] ","color":"dark_aqua"}
execute if score #old_loaded cs2_const matches 1 unless score #cast_ready cs2_const matches 1 run scoreboard objectives add cs2_join_cast trigger
execute if score #old_loaded cs2_const matches 1 unless score #cast_ready cs2_const matches 1 run scoreboard players set #cast_ready cs2_const 1
# 侧边栏已停用；清理旧存档可能残留的侧边栏显示
scoreboard objectives setdisplay sidebar
# 老玩家/单人档 reload 后补上个人 HUD 开关分数
execute as @a unless score @s cs2_hud matches 0..1 run scoreboard players set @s cs2_hud 1

# 已在线玩家（尤其是单人档 /reload）重新启用入队触发器。
execute if entity @a run scoreboard players enable @a cs2_join_t
execute if entity @a run scoreboard players enable @a cs2_join_ct
execute if entity @a run scoreboard players enable @a cs2_join_cast
execute if entity @a run scoreboard players enable @a cs2_leave
execute if entity @a run scoreboard players enable @a cs2_hud_toggle
execute if entity @a run scoreboard players enable @a cs2_stats
execute if entity @a run scoreboard players enable @a cs2_panel
execute if entity @a run scoreboard players enable @a cs2_pause
execute if entity @a run scoreboard players enable @a cs2_vote_restart
execute if entity @a run scoreboard players enable @a cs2_vote_end
execute if entity @a run scoreboard players enable @a cs2_practice_leave
execute if entity @a run scoreboard players enable @a cs2_train_hold
execute if entity @a run scoreboard players enable @a cs2_train_spawn
execute if entity @a run scoreboard players enable @a cs2_match_pause
execute if entity @a run scoreboard players enable @a cs2_match_resume
# 升级清理：旧版商店木棍不再使用，移除在线玩家背包里的旧道具
clear @a[scores={cs2_inmatch=1}] minecraft:stick[minecraft:custom_data~{cs2_shop_item:1b}]
# 多地图系统：记录当前地图名；老存档首次 reload 时初始化为未选择
execute unless data storage cs2:map active run data modify storage cs2:map active set value "未选择"
function cs2:load/config
# 回滚：死亡不掉落模式
gamerule keepInventory true

# 友伤始终开启；这里只保证队伍状态与全局 PvP 一致
execute if score #state cs2_state matches 0 run function cs2:pvp/on
execute if score #state cs2_state matches 7 run function cs2:pvp/on
execute if score #state cs2_state matches 1..6 run function cs2:pvp/off
# 拆弹器改为纯标记：清掉旧版遗留在背包里的剪刀
execute as @a run clear @s minecraft:shears[minecraft:custom_data={cs2_kit:1b}]

# C4 音效信号（独立标记，避免被 panel_ready 挡住）
execute unless score #sfx_ready cs2_const matches 1 run scoreboard objectives add cs2_sfx dummy
execute unless score #sfx_ready cs2_const matches 1 run scoreboard players set #sfx_ready cs2_const 1
# C4 蜂鸣计时
execute unless score #beep_ready cs2_const matches 1 run scoreboard objectives add cs2_beep dummy
execute unless score #beep_ready cs2_const matches 1 run scoreboard players set #beep_ready cs2_const 1
