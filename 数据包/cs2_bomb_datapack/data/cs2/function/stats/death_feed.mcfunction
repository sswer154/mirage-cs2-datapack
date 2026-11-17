# 死亡播报：仅发给比赛参赛者；@s 是死者
scoreboard players set @s cs2_death_pending 0
execute unless score @s cs2_inmatch matches 1 run return 0
# 参赛者 + 解说队伍都能看到播报
tag @a[scores={cs2_inmatch=1}] add cs2_feed_target
tag @a[team=cs2_cast] add cs2_feed_target

# 没有击杀者：坠落/爆炸等
execute unless entity @a[tag=cs2_recent_killer,limit=1] if entity @s[team=cs2_t] run tellraw @a[tag=cs2_feed_target] [{"text":"[T] ","color":"red"},{"selector":"@s"},{"text":" 阵亡","color":"gray"}]
execute unless entity @a[tag=cs2_recent_killer,limit=1] if entity @s[team=cs2_ct] run tellraw @a[tag=cs2_feed_target] [{"text":"[CT] ","color":"blue"},{"selector":"@s"},{"text":" 阵亡","color":"gray"}]

# 击杀者在 T
execute if entity @a[tag=cs2_recent_killer,team=cs2_t,limit=1] if entity @a[tag=cs2_tk_recent,limit=1] if entity @s[team=cs2_t] run tellraw @a[tag=cs2_feed_target] [{"text":"[友伤] ","color":"dark_red"},{"text":"[T] ","color":"red"},{"selector":"@a[tag=cs2_recent_killer,team=cs2_t,limit=1]"},{"text":" 击杀了 ","color":"gray"},{"text":"[T] ","color":"red"},{"selector":"@s"}]
execute if entity @a[tag=cs2_recent_killer,team=cs2_t,limit=1] if entity @a[tag=cs2_tk_recent,limit=1] if entity @s[team=cs2_ct] run tellraw @a[tag=cs2_feed_target] [{"text":"[友伤] ","color":"dark_red"},{"text":"[T] ","color":"red"},{"selector":"@a[tag=cs2_recent_killer,team=cs2_t,limit=1]"},{"text":" 击杀了 ","color":"gray"},{"text":"[CT] ","color":"blue"},{"selector":"@s"}]
execute if entity @a[tag=cs2_recent_killer,team=cs2_t,limit=1] unless entity @a[tag=cs2_tk_recent,limit=1] if entity @s[team=cs2_t] run tellraw @a[tag=cs2_feed_target] [{"text":"[T] ","color":"red"},{"selector":"@a[tag=cs2_recent_killer,team=cs2_t,limit=1]"},{"text":" 击杀了 ","color":"gray"},{"text":"[T] ","color":"red"},{"selector":"@s"}]
execute if entity @a[tag=cs2_recent_killer,team=cs2_t,limit=1] unless entity @a[tag=cs2_tk_recent,limit=1] if entity @s[team=cs2_ct] run tellraw @a[tag=cs2_feed_target] [{"text":"[T] ","color":"red"},{"selector":"@a[tag=cs2_recent_killer,team=cs2_t,limit=1]"},{"text":" 击杀了 ","color":"gray"},{"text":"[CT] ","color":"blue"},{"selector":"@s"}]

# 击杀者在 CT
execute if entity @a[tag=cs2_recent_killer,team=cs2_ct,limit=1] if entity @a[tag=cs2_tk_recent,limit=1] if entity @s[team=cs2_t] run tellraw @a[tag=cs2_feed_target] [{"text":"[友伤] ","color":"dark_red"},{"text":"[CT] ","color":"blue"},{"selector":"@a[tag=cs2_recent_killer,team=cs2_ct,limit=1]"},{"text":" 击杀了 ","color":"gray"},{"text":"[T] ","color":"red"},{"selector":"@s"}]
execute if entity @a[tag=cs2_recent_killer,team=cs2_ct,limit=1] if entity @a[tag=cs2_tk_recent,limit=1] if entity @s[team=cs2_ct] run tellraw @a[tag=cs2_feed_target] [{"text":"[友伤] ","color":"dark_red"},{"text":"[CT] ","color":"blue"},{"selector":"@a[tag=cs2_recent_killer,team=cs2_ct,limit=1]"},{"text":" 击杀了 ","color":"gray"},{"text":"[CT] ","color":"blue"},{"selector":"@s"}]
execute if entity @a[tag=cs2_recent_killer,team=cs2_ct,limit=1] unless entity @a[tag=cs2_tk_recent,limit=1] if entity @s[team=cs2_t] run tellraw @a[tag=cs2_feed_target] [{"text":"[CT] ","color":"blue"},{"selector":"@a[tag=cs2_recent_killer,team=cs2_ct,limit=1]"},{"text":" 击杀了 ","color":"gray"},{"text":"[T] ","color":"red"},{"selector":"@s"}]
execute if entity @a[tag=cs2_recent_killer,team=cs2_ct,limit=1] unless entity @a[tag=cs2_tk_recent,limit=1] if entity @s[team=cs2_ct] run tellraw @a[tag=cs2_feed_target] [{"text":"[CT] ","color":"blue"},{"selector":"@a[tag=cs2_recent_killer,team=cs2_ct,limit=1]"},{"text":" 击杀了 ","color":"gray"},{"text":"[CT] ","color":"blue"},{"selector":"@s"}]

# 一次播报后清理，防止下一次死亡被旧标签污染
tag @a remove cs2_recent_killer
tag @a remove cs2_tk_recent
tag @a remove cs2_feed_target
