# 注意：这里不能每 tick 把 cs2_defuse 清零，否则永远达不到阈值。
# 进度由 defuse_check 在“离开范围/松开 Shift/阵亡”时清零，持续潜行时累加。
execute as @a[team=cs2_ct,scores={cs2_alive=1}] at @s run function cs2:bomb/defuse_check

# 拆包阈值：普通 defuse 秒；拆弹器 (defuse - kit_time) 秒
scoreboard players operation #defuse_normal cs2_counts = defuse cs2_cfg
scoreboard players operation #defuse_normal cs2_counts *= #20 cs2_const
scoreboard players operation #defuse_tmp cs2_counts = kit_time cs2_cfg
scoreboard players operation #defuse_tmp cs2_counts *= #20 cs2_const
scoreboard players operation #defuse_kit cs2_counts = #defuse_normal cs2_counts
scoreboard players operation #defuse_kit cs2_counts -= #defuse_tmp cs2_counts

execute as @a[team=cs2_ct,scores={cs2_kit=1}] if score @s cs2_defuse >= #defuse_kit cs2_counts run function cs2:bomb/defused
execute as @a[team=cs2_ct,scores={cs2_kit=0}] if score @s cs2_defuse >= #defuse_normal cs2_counts run function cs2:bomb/defused
