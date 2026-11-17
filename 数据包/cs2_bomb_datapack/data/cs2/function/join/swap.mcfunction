# 中场/加时换边：按 cs2_team 选择，包含已阵亡但保留队伍的玩家
tag @a[scores={cs2_team=1}] add cs2_swap_t
tag @a[scores={cs2_team=2}] add cs2_swap_ct
execute as @a[tag=cs2_swap_t] run team join cs2_ct @s
execute as @a[tag=cs2_swap_t] run scoreboard players set @s cs2_team 2
execute as @a[tag=cs2_swap_ct] run team join cs2_t @s
execute as @a[tag=cs2_swap_ct] run scoreboard players set @s cs2_team 1
tag @a remove cs2_swap_t
tag @a remove cs2_swap_ct
execute as @a run function cs2:player/tag
