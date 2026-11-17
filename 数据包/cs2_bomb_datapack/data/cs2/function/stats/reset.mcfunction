# 每场比赛开始时重置本场 K/D；deathCount 用 reset 归零
scoreboard players set @a[scores={cs2_inmatch=1}] cs2_kills 0
scoreboard players reset @a[scores={cs2_inmatch=1}] cs2_deaths
scoreboard players set @a[scores={cs2_inmatch=1}] cs2_prev_deaths 0
scoreboard players set @a[scores={cs2_inmatch=1}] cs2_death_pending 0
scoreboard players set @a[scores={cs2_inmatch=1}] cs2_killer_timer 0
tag @a remove cs2_recent_killer
tag @a remove cs2_tk_recent
scoreboard players set #top_kills cs2_econ 0
