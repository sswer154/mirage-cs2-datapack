# 首次运行或删掉 cs2_cfg 后写入默认配置。
# 想改默认值，改这里，然后执行 /function cs2:config/defaults。
scoreboard players set warmup cs2_cfg 10
scoreboard players set freeze cs2_cfg 25
scoreboard players set round cs2_cfg 115
scoreboard players set round_end cs2_cfg 5
scoreboard players set bomb cs2_cfg 40
scoreboard players set defuse cs2_cfg 10
scoreboard players set kit_time cs2_cfg 5
scoreboard players set kit cs2_cfg 1
scoreboard players set overtime cs2_cfg 1
scoreboard players set #music_preset cs2_cfg 1

# 内部状态初始化（只在首次写默认值时执行）
scoreboard players set #state cs2_state 0
scoreboard players set #timer cs2_timer 0
scoreboard players set #round cs2_round 0
scoreboard players set #t cs2_score_t 0
scoreboard players set #ct cs2_score_ct 0
scoreboard players set #target cs2_score_t 13
scoreboard players set #half cs2_bomb_state 0
scoreboard players set #ot cs2_bomb_state 0
scoreboard players set #ot_half cs2_bomb_state 0
scoreboard players set #bomb cs2_bomb_state 0
scoreboard players set #bomb_timer cs2_bomb_timer 0
scoreboard players set #site cs2_site 0
scoreboard players set #winner cs2_counts 0
scoreboard players set #reason cs2_counts 0
scoreboard players set #drop_timer cs2_counts 0
scoreboard players set #hud_tick cs2_const 0
scoreboard players set #blink cs2_blink 0
scoreboard players set #blink_on cs2_blink 0
scoreboard players set #cfg_ready cs2_const 1
