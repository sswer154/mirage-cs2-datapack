scoreboard players reset @s cs2_vote_restart
scoreboard players enable @s cs2_vote_restart
execute if score #paused cs2_counts matches 1 run tellraw @s {"text":"暂停中不能投票。","color":"red"}
execute if score #paused cs2_counts matches 1 run return 0
execute unless score #state cs2_state matches 3..4 run tellraw @s {"text":"只有回合进行中才能投票重开。","color":"red"}
execute unless score #state cs2_state matches 3..4 run return 0
execute unless score #solo cs2_counts matches 0 run tellraw @s {"text":"练习模式不能使用重开投票。","color":"red"}
execute unless score #solo cs2_counts matches 0 run return 0
execute unless score @s cs2_inmatch matches 1 run tellraw @s {"text":"你不是本场参赛者。","color":"red"}
execute unless score @s cs2_inmatch matches 1 run return 0
execute if entity @s[tag=cs2_voted_restart] run tellraw @s {"text":"你已经同意重开本小局了。","color":"yellow"}
execute if entity @s[tag=cs2_voted_restart] run return 0
tag @s add cs2_voted_restart
execute store result score #vote_restart_yes cs2_counts if entity @a[tag=cs2_voted_restart,scores={cs2_inmatch=1}]
execute store result score #vote_restart_total cs2_counts if entity @a[scores={cs2_inmatch=1}]
tellraw @a[scores={cs2_inmatch=1}] [{"text":"重开投票：","color":"green"},{"text":"已同意 ","color":"gray"},{"score":{"name":"#vote_restart_yes","objective":"cs2_counts"}},{"text":"/","color":"gray"},{"score":{"name":"#vote_restart_total","objective":"cs2_counts"}},{"text":" 人","color":"gray"}]
execute if score #vote_restart_total cs2_counts matches 1.. if score #vote_restart_yes cs2_counts >= #vote_restart_total cs2_counts run function cs2:panel/restart
