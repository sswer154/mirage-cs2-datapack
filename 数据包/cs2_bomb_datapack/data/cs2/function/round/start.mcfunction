# 回合开始：冻结阶段
scoreboard players set #state cs2_state 2
function cs2:pvp/off
scoreboard players set #bomb cs2_bomb_state 0
scoreboard players set #bomb_timer cs2_bomb_timer 0
scoreboard players set #site cs2_site 0
scoreboard players set #drop_timer cs2_counts 0
scoreboard players set #blink cs2_blink 0
scoreboard players set #blink_on cs2_blink 0
function cs2:econ/round_start
function cs2:panel/reset_votes

function cs2:player/round_all
tp @a[scores={cs2_inmatch=1,cs2_team=1}] @e[type=marker,tag=cs2_spawn_t,limit=1]
tp @a[scores={cs2_inmatch=1,cs2_team=2}] @e[type=marker,tag=cs2_spawn_ct,limit=1]
tp @a[scores={cs2_inmatch=1,cs2_team=0}] @e[type=marker,tag=cs2_lobby,limit=1]
function cs2:round/freeze
