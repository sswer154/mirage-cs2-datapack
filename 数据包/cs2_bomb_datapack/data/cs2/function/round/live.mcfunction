execute if score #solo cs2_counts matches 1..2 if entity @a[team=cs2_t,scores={cs2_inmatch=1}] if entity @a[team=cs2_ct,scores={cs2_inmatch=1}] run return run function cs2:practice/to_formal
scoreboard players set #state cs2_state 3
# 倒计时结束，强制关闭还开着的商店界面
execute as @a run cs2shop close
clear @a[scores={cs2_inmatch=1}] minecraft:diamond[minecraft:custom_data~{cs2_shop_item:1b}]
clear @a[scores={cs2_inmatch=1}] minecraft:stick[minecraft:custom_data~{cs2_shop_item:1b}]
# 回合进行阶段恢复 PvP
function cs2:pvp/round_live
# 回滚：对局内保持死亡不掉落
gamerule keepInventory true
# 准备阶段已在商店购买装备；这里只切换状态、计时与炸弹
# 开赛时确定是否为练习局。正式赛保留模式，练习局跟随当前阵营。
execute if score #solo cs2_counts matches 1..2 if entity @a[team=cs2_ct,scores={cs2_inmatch=1}] unless entity @a[team=cs2_t,scores={cs2_inmatch=1}] run scoreboard players set #solo cs2_counts 2
execute if score #solo cs2_counts matches 1..2 if entity @a[team=cs2_t,scores={cs2_inmatch=1}] unless entity @a[team=cs2_ct,scores={cs2_inmatch=1}] run scoreboard players set #solo cs2_counts 1
scoreboard players operation #timer cs2_timer = round cs2_cfg
scoreboard players operation #timer cs2_timer *= #20 cs2_const
scoreboard players set #bomb cs2_bomb_state 0
scoreboard players set #blink cs2_blink 0
scoreboard players set #blink_on cs2_blink 0

bossbar set cs2:round visible true
bossbar set cs2:bomb visible false
execute store result bossbar cs2:round max run scoreboard players get #timer cs2_timer
execute store result bossbar cs2:round value run scoreboard players get #timer cs2_timer

effect clear @a[scores={cs2_inmatch=1}] minecraft:slowness
effect clear @a[scores={cs2_inmatch=1}] minecraft:resistance
execute as @a[scores={cs2_inmatch=1}] run function cs2:util/jump_unlock

gamemode survival @a[team=cs2_t,scores={cs2_inmatch=1,cs2_alive=1}]
gamemode survival @a[team=cs2_ct,scores={cs2_inmatch=1,cs2_alive=1}]

# 清理离线玩家留在计分板上的上回合携带状态，再发本回合 C4。
scoreboard players reset * cs2_carrier
function cs2:loadout/give_bomb

title @a[scores={cs2_inmatch=1}] times 10 30 10
title @a[scores={cs2_inmatch=1,cs2_hud=1}] title {"text":"开始!","color":"green","bold":true}
title @a[scores={cs2_inmatch=1,cs2_hud=1}] subtitle {"text":"消灭敌人或安放炸弹","color":"gray"}
playsound minecraft:entity.ender_dragon.growl master @a[scores={cs2_inmatch=1}]
tellraw @a[scores={cs2_inmatch=1}] [{"text":"[战术菜单]","color":"aqua","clickEvent":{"action":"run_command","value":"/trigger cs2_panel set 1"},"hoverEvent":{"action":"show_text","value":{"text":"暂停 / 重开 / 结束投票"}}}]

# 变成正式战局：回收测试商店并封锁
execute if score #solo cs2_counts matches 0 run clear @a[scores={cs2_inmatch=1}] minecraft:emerald[minecraft:custom_data~{cs2_shop_item:1b}]
