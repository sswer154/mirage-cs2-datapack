# 回合开始：只处理本场参赛者（cs2_inmatch=1）
# 上局存活者保留武器/护甲/拆弹器；阵亡或新加入者清包并重新发默认装备
execute unless score @s cs2_inmatch matches 1 run return 0

# 先保存上局是否存活，再覆盖 cs2_alive
scoreboard players operation @s cs2_was_alive = @s cs2_alive

execute if score @s cs2_team matches 1 run team join cs2_t @s
execute if score @s cs2_team matches 2 run team join cs2_ct @s

execute if score @s cs2_team matches 1..2 run scoreboard players set @s cs2_alive 1
execute unless score @s cs2_team matches 1..2 run scoreboard players set @s cs2_alive 0

# 回合计时/炸弹相关的临时状态，每回合都清
scoreboard players set @s cs2_carrier 0
scoreboard players set @s cs2_defuse 0
scoreboard players set @s cs2_planter 0

# 存活者保留武器/护甲，但 C4 不能跨回合携带；下一局由随机 T 重新领取
clear @s minecraft:nautilus_shell[minecraft:custom_data={cs2_bomb:1b}]

# 只有阵亡、新加入或已离队的人才清背包和购买状态
execute if score @s cs2_team matches 0 run function cs2:player/round_reset_clear
execute if score @s cs2_team matches 1..2 unless score @s cs2_was_alive matches 1 run function cs2:player/round_reset_clear

# 存活者保留装备，但换边设置的补发标记要保留到发装时。
execute if score @s cs2_team matches 0 run scoreboard players set @s cs2_needs_default 0
# 只清除准备阶段的限制效果，保留玩家自己的其他 buff
effect clear @s minecraft:slowness
effect clear @s minecraft:resistance
function cs2:player/restore_health

function cs2:player/tag
scoreboard players operation @s cs2_prev_deaths = @s cs2_deaths

execute if score @s cs2_team matches 1..2 run gamemode adventure @s
execute if score @s cs2_team matches 0 run gamemode spectator @s

# 装备清点/补发之后再快照，避免把马上要被清掉的东西算进本回合名额
function cs2:shop/snapshot_carry
