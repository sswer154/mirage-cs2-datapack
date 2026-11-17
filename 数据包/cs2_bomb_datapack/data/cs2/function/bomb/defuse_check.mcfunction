# 单个 CT 的拆弹检测（由 defuse_tick 以 as @s at @s 调用）
# 用 sneak_time 统计的每 tick 增量判断“正在潜行”，不依赖玩家 NBT 的 Sneaking 字段。
scoreboard players operation @s cs2_sneak_delta = @s cs2_sneak
scoreboard players operation @s cs2_sneak_delta -= @s cs2_sneak_prev
scoreboard players operation @s cs2_sneak_prev = @s cs2_sneak

# 不在炸弹 3 格内：清零进度，不提示，直接返回
execute at @s unless entity @e[type=marker,tag=cs2_bomb,limit=1,distance=..3] run scoreboard players set @s cs2_defuse 0
execute at @s unless entity @e[type=marker,tag=cs2_bomb,limit=1,distance=..3] run return 0

# 在范围内但没潜行：清零进度，提示按住 Shift
execute unless score @s cs2_sneak_delta matches 1.. run scoreboard players set @s cs2_defuse 0
execute unless score @s cs2_sneak_delta matches 1.. run title @s actionbar {"text":"靠近炸弹后按住 Shift 拆弹","color":"gray"}
execute unless score @s cs2_sneak_delta matches 1.. run return 0

# 正在潜行：显示进度提示并累计（按 sneak_time 的实际增量，兼容统计批量更新）
title @s actionbar {"text":"拆弹中...","color":"yellow"}
scoreboard players operation @s cs2_defuse += @s cs2_sneak_delta
