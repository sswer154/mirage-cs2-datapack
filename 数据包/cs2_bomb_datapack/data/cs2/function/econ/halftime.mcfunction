# 下半场换边后回到手枪局：金钱 $800、连败奖励 1900，清空装备与购买状态
function cs2:econ/start
execute as @a[scores={cs2_inmatch=1}] run function cs2:player/round_reset_clear
execute as @a[scores={cs2_inmatch=1}] run scoreboard players set @s cs2_alive 0
execute as @a[scores={cs2_inmatch=1}] run scoreboard players set @s cs2_was_alive 0
execute as @a[scores={cs2_inmatch=1}] run function cs2:util/speed
