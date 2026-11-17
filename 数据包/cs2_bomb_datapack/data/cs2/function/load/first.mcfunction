# 首次安装时执行一次：创建计分板、队伍、BossBar、HUD。
# 这些内容在 /reload 后会保留，所以不需要重复创建。

scoreboard objectives add cs2_state dummy
scoreboard objectives add cs2_timer dummy
scoreboard objectives add cs2_round dummy
scoreboard objectives add cs2_score_t dummy
scoreboard objectives add cs2_score_ct dummy
scoreboard objectives add cs2_site dummy
scoreboard objectives add cs2_bomb_state dummy
scoreboard objectives add cs2_bomb_timer dummy
scoreboard objectives add cs2_defuse dummy
scoreboard objectives add cs2_carrier dummy
scoreboard objectives add cs2_alive dummy
scoreboard objectives add cs2_kit dummy
scoreboard objectives add cs2_team dummy
scoreboard objectives add cs2_seen dummy
scoreboard objectives add cs2_init dummy
scoreboard objectives add cs2_prev_deaths dummy
scoreboard objectives add cs2_blink dummy
scoreboard objectives add cs2_counts dummy
scoreboard objectives add cs2_cfg dummy
scoreboard objectives add cs2_hud dummy
scoreboard objectives add cs2_deaths deathCount
scoreboard objectives add cs2_kills dummy
scoreboard objectives add cs2_death_pending dummy
scoreboard objectives add cs2_killer_timer dummy
scoreboard objectives add cs2_stats trigger
scoreboard objectives add cs2_join_t trigger
scoreboard objectives add cs2_join_ct trigger
scoreboard objectives add cs2_join_cast trigger
scoreboard objectives add cs2_leave trigger
scoreboard objectives add cs2_util_smoke dummy
scoreboard objectives add cs2_util_he dummy
scoreboard objectives add cs2_util_flash dummy
scoreboard objectives add cs2_util_fire dummy

team add cs2_t
team modify cs2_t color red
team modify cs2_t friendlyFire false
team modify cs2_t seeFriendlyInvisibles true
team modify cs2_t nametagVisibility hideForOtherTeams
team modify cs2_t deathMessageVisibility hideForOtherTeams

team add cs2_ct
team modify cs2_ct color blue
team modify cs2_ct friendlyFire false
team modify cs2_ct seeFriendlyInvisibles true
team modify cs2_ct nametagVisibility hideForOtherTeams
team modify cs2_ct deathMessageVisibility hideForOtherTeams

team add cs2_cast
team modify cs2_cast color dark_aqua
team modify cs2_cast friendlyFire false
team modify cs2_cast seeFriendlyInvisibles true
team modify cs2_cast nametagVisibility hideForOtherTeams
team modify cs2_cast deathMessageVisibility hideForOtherTeams
team modify cs2_cast collisionRule never
team modify cs2_cast prefix {"text":"[解说] ","color":"dark_aqua"}

bossbar add cs2:round {"text":"回合剩余"}
bossbar set cs2:round color blue
bossbar set cs2:round style notched_20
bossbar set cs2:round max 2300
bossbar set cs2:round visible false

bossbar add cs2:bomb {"text":"炸弹倒计时"}
bossbar set cs2:bomb color red
bossbar set cs2:bomb style notched_20
bossbar set cs2:bomb max 800
bossbar set cs2:bomb visible false

gamerule doImmediateRespawn true

scoreboard players set #20 cs2_const 20
scoreboard players set #tick cs2_const 0
scoreboard players set #loaded cs2_const 1
scoreboard players set #cast_ready cs2_const 1
