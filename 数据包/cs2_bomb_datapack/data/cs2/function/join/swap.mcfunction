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

# 换边：清掉双方阵营的默认手枪，并按新阵营重新发放，避免旧枪残留＋重复发放
execute as @a run clear @s tacz:modern_kinetic_gun[minecraft:custom_data~{GunId:"tacz:glock_17"}]
execute as @a run clear @s tacz:modern_kinetic_gun[minecraft:custom_data~{GunId:"tacz:m9a4"}]
scoreboard players set @a cs2_secondary 0
scoreboard players set @a cs2_needs_default 1
