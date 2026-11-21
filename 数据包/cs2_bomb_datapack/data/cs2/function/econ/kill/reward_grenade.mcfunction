# 高爆手雷与燃烧弹的敌方击杀使用默认奖金，不读当前主手。
execute unless score #state cs2_state matches 3..4 run return 0
execute if entity @s[team=cs2_ct] run scoreboard players add #ct_kills cs2_econ 1
scoreboard players set #kill_money cs2_econ 300
function cs2:econ/kill/pay
