# 只重置比赛状态，不动地图 marker 和配置
scoreboard players set #state cs2_state 0
schedule clear cs2:round/play_ct_win
kill @e[type=item,tag=cs2_match_loot]
scoreboard players set #timer cs2_timer 0
scoreboard players set #round cs2_round 0
scoreboard players set #round_restarting cs2_counts 0
scoreboard players set #round_replayed cs2_counts 0
scoreboard players reset #checkpoint cs2_health_round
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
scoreboard players set #break_type cs2_counts 0
scoreboard players set #solo cs2_counts 0
scoreboard players set #train_hold cs2_counts 0
scoreboard players set #hud_tick cs2_const 0
scoreboard players set #blink cs2_blink 0
scoreboard players set #blink_on cs2_blink 0
bossbar set cs2:round visible false
bossbar set cs2:bomb visible false
function cs2:econ/reset
function cs2:panel/reset
