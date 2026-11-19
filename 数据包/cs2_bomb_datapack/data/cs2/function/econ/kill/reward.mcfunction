# 击杀奖励：按致死武器的 GunId 发奖；未匹配则按手枪/其他 200
scoreboard players set #kill_money cs2_econ 200
execute if data storage cs2:kill_reward {gun:"tacz:hk_mp5a5"} run scoreboard players set #kill_money cs2_econ 400
execute if data storage cs2:kill_reward {gun:"tacz:ump45"} run scoreboard players set #kill_money cs2_econ 400
execute if data storage cs2:kill_reward {gun:"bf1:vp1915"} run scoreboard players set #kill_money cs2_econ 400
execute if data storage cs2:kill_reward {gun:"tacz:p90"} run scoreboard players set #kill_money cs2_econ 400
execute if data storage cs2:kill_reward {gun:"tacz:m870"} run scoreboard players set #kill_money cs2_econ 600
execute if data storage cs2:kill_reward {gun:"tacz:aa12"} run scoreboard players set #kill_money cs2_econ 600
execute if data storage cs2:kill_reward {gun:"tacz:db_long"} run scoreboard players set #kill_money cs2_econ 600
execute if data storage cs2:kill_reward {gun:"tacz:m1014"} run scoreboard players set #kill_money cs2_econ 600
execute if data storage cs2:kill_reward {gun:"bf1:m2_2"} run scoreboard players set #kill_money cs2_econ 600
execute if data storage cs2:kill_reward {gun:"tacz:ai_awp"} run scoreboard players set #kill_money cs2_econ 100
execute if data storage cs2:kill_reward {gun:"claymore:zerotime"} run scoreboard players set #kill_money cs2_econ 1500
function cs2:econ/kill/pay
