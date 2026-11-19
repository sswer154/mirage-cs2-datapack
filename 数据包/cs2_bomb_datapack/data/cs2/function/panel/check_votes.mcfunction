# 离开游戏或队伍也可能使投票达到全票。
function cs2:panel/recount_votes
execute unless score #vote_total cs2_counts matches 1.. run return 0
execute if score #state cs2_state matches 3..4 if score #vote_restart_yes cs2_counts = #vote_total cs2_counts run return run function cs2:panel/restart
execute if score #vote_end_yes cs2_counts = #vote_total cs2_counts run function cs2:panel/end
