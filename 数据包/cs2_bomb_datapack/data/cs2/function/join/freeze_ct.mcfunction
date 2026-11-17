team join cs2_ct @s
scoreboard players set @s cs2_team 2
scoreboard players set @s cs2_alive 1
scoreboard players set @s cs2_carrier 0
scoreboard players set @s cs2_kit 0
scoreboard players set @s cs2_money 800
scoreboard players set @s cs2_primary 0
scoreboard players set @s cs2_secondary 0
scoreboard players set @s cs2_armor 0
scoreboard players set @s cs2_planter 0
scoreboard players set @s cs2_util_smoke 0
scoreboard players set @s cs2_util_he 0
scoreboard players set @s cs2_util_flash 0
scoreboard players set @s cs2_util_fire 0
scoreboard players set @s cs2_wclass 1
scoreboard players set @s cs2_inmatch 1
scoreboard players set @s cs2_needs_default 0
scoreboard players set @s cs2_was_alive 0
clear @s
effect clear @s
function cs2:util/speed
function cs2:util/freeze_lock
gamemode adventure @s
function cs2:player/tag
execute if entity @e[type=marker,tag=cs2_spawn_ct] run tp @s @e[type=marker,tag=cs2_spawn_ct,limit=1]
function cs2:shop/default_ct
title @s title {"text":"已加入 CT","color":"blue"}

# 中途加入/重连：重置本回合购买计数，并按当前背包快照计入上限
scoreboard players set @s cs2_item_count 0
function cs2:shop/snapshot_carry

# 单人测试局：额外发一个测试商店
execute if score #state cs2_state matches 2 if score #solo cs2_counts matches 1..2 run function cs2:util/give_test_shop
