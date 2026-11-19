scoreboard players reset @s cs2_vote_end
scoreboard players enable @s cs2_vote_end
execute if score #paused cs2_counts matches 1 run tellraw @s {"text":"暂停中不能投票。","color":"red"}
execute if score #paused cs2_counts matches 1 run return 0
execute unless score #state cs2_state matches 2..4 run tellraw @s {"text":"只有准备阶段或回合进行中才能投票结束战局。","color":"red"}
execute unless score #state cs2_state matches 2..4 run return 0
execute unless score #solo cs2_counts matches 0 run tellraw @s {"text":"练习模式不能使用结束投票。","color":"red"}
execute unless score #solo cs2_counts matches 0 run return 0
execute unless score @s cs2_inmatch matches 1 run tellraw @s {"text":"你不是本场参赛者。","color":"red"}
execute unless score @s cs2_inmatch matches 1 run return 0
execute if score @s cs2_voted_end matches 1 run tellraw @s {"text":"你已经同意结束战局了。","color":"yellow"}
execute if score @s cs2_voted_end matches 1 run return 0
scoreboard players set @s cs2_voted_end 1
function cs2:panel/recount_votes
tellraw @a[scores={cs2_inmatch=1}] [{"text":"结束战局投票：","color":"gold"},{"text":"已同意 ","color":"gray"},{"score":{"name":"#vote_end_yes","objective":"cs2_counts"}},{"text":"/","color":"gray"},{"score":{"name":"#vote_total","objective":"cs2_counts"}},{"text":" 人","color":"gray"}]
