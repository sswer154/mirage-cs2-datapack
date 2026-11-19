# 已参赛者选回当前阵营，相当于取消尚未生效的换队。
$execute if score @s cs2_inmatch matches 1 if score @s cs2_team matches $(side) if score @s cs2_queued_team matches 1..2 run return run function cs2:join/cancel_queue
$execute if score @s cs2_inmatch matches 1 if score @s cs2_team matches $(side) run return run tellraw @s {"text":"你已经在 $(label) 队。","color":"gray"}

# 首次排队时保存个人背包。更改排队阵营时沿用原备份。
execute unless score @s cs2_inmatch matches 1 unless score @s cs2_queued_team matches 1..2 unless function cs2:join/queue_wait run return fail
$scoreboard players set @s cs2_queued_team $(side)
scoreboard players set @s cs2_queue_round 0
scoreboard players operation @s cs2_queue_round = #checkpoint cs2_health_round
scoreboard players enable @s cs2_cancel_queue
$tellraw @s [{"text":"正在等待比赛，将于下一正常回合加入 $(label)。","color":"yellow"},{"text":" [取消等待]","color":"gray","clickEvent":{"action":"run_command","value":"/trigger cs2_cancel_queue"}}]
