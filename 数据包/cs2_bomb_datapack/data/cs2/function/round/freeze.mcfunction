# 准备阶段开始：先恢复死亡不掉落，再清理地面掉落物
gamerule keepInventory true
kill @e[type=item]
# 清理上一局的烟雾
kill @e[type=marker,tag=cs2_smoke]
kill @e[type=marker,tag=cs2_fire]

scoreboard players operation #timer cs2_timer = freeze cs2_cfg
scoreboard players operation #timer cs2_timer *= #20 cs2_const
bossbar set cs2:round visible true
execute store result bossbar cs2:round max run scoreboard players get #timer cs2_timer
execute store result bossbar cs2:round value run scoreboard players get #timer cs2_timer
bossbar set cs2:bomb visible false

title @a[scores={cs2_inmatch=1}] times 10 40 10
title @a[scores={cs2_inmatch=1,cs2_hud=1}] title [{"text":"第 ","color":"gold"},{"score":{"name":"#round","objective":"cs2_round"}},{"text":" 回合","color":"gold"}]
title @a[scores={cs2_inmatch=1,cs2_hud=1}] subtitle {"text":"准备阶段 · 用商店钻石购买装备","color":"gray"}
# 只有本回合需要默认装备的人（阵亡/新加入）才发默认手枪，存活者保留自己的装备
execute as @a[team=cs2_t,scores={cs2_inmatch=1,cs2_needs_default=1}] run function cs2:shop/default_t
execute as @a[team=cs2_ct,scores={cs2_inmatch=1,cs2_needs_default=1}] run function cs2:shop/default_ct
scoreboard players set @a[scores={cs2_inmatch=1,cs2_needs_default=1}] cs2_needs_default 0
# 兜底检查：即使 needs_default 被误置 0，也确保默认手枪/弹药盒确实在背包里
execute as @a[team=cs2_t,scores={cs2_inmatch=1}] run function cs2:shop/ensure_default_t
execute as @a[team=cs2_ct,scores={cs2_inmatch=1}] run function cs2:shop/ensure_default_ct
execute as @a[scores={cs2_inmatch=1}] run function cs2:util/give_shop_item
# 每小局开始显示一次本局 K/D
execute as @a[scores={cs2_inmatch=1}] run function cs2:stats/round_start
tellraw @a[scores={cs2_inmatch=1}] [{"text":"[战术菜单]","color":"aqua","clickEvent":{"action":"run_command","value":"/trigger cs2_panel set 1"},"hoverEvent":{"action":"show_text","value":{"text":"暂停 / 重开 / 结束投票"}}}]

execute as @a[scores={cs2_inmatch=1}] run function cs2:util/speed
execute as @a[scores={cs2_inmatch=1}] run function cs2:util/freeze_lock

gamemode adventure @a[team=cs2_t,scores={cs2_inmatch=1}]
gamemode adventure @a[team=cs2_ct,scores={cs2_inmatch=1}]
playsound minecraft:block.note_block.pling master @a[scores={cs2_inmatch=1}]

# 单人测试局：额外发一个测试商店
execute if score #solo cs2_counts matches 1..2 as @a[scores={cs2_inmatch=1}] run function cs2:util/give_test_shop
execute unless score #solo cs2_counts matches 1..2 run clear @a minecraft:emerald[minecraft:custom_data~{cs2_shop_item:1b}]
