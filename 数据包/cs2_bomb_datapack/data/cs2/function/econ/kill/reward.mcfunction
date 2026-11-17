# 击杀奖励：按主手 TACZ 枪的 GunId 发奖；未匹配则按手枪/其他 200
scoreboard players set #kill_money cs2_econ 200
execute if items entity @s weapon.mainhand tacz:modern_kinetic_gun[minecraft:custom_data~{GunId:"tacz:hk_mp5a5"}] run scoreboard players set #kill_money cs2_econ 400
execute if items entity @s weapon.mainhand tacz:modern_kinetic_gun[minecraft:custom_data~{GunId:"tacz:ump45"}] run scoreboard players set #kill_money cs2_econ 400
execute if items entity @s weapon.mainhand tacz:modern_kinetic_gun[minecraft:custom_data~{GunId:"bf1:vp1915"}] run scoreboard players set #kill_money cs2_econ 400
execute if items entity @s weapon.mainhand tacz:modern_kinetic_gun[minecraft:custom_data~{GunId:"tacz:p90"}] run scoreboard players set #kill_money cs2_econ 400
execute if items entity @s weapon.mainhand tacz:modern_kinetic_gun[minecraft:custom_data~{GunId:"tacz:m870"}] run scoreboard players set #kill_money cs2_econ 600
execute if items entity @s weapon.mainhand tacz:modern_kinetic_gun[minecraft:custom_data~{GunId:"tacz:aa12"}] run scoreboard players set #kill_money cs2_econ 600
execute if items entity @s weapon.mainhand tacz:modern_kinetic_gun[minecraft:custom_data~{GunId:"tacz:db_long"}] run scoreboard players set #kill_money cs2_econ 600
execute if items entity @s weapon.mainhand tacz:modern_kinetic_gun[minecraft:custom_data~{GunId:"tacz:m1014"}] run scoreboard players set #kill_money cs2_econ 600
execute if items entity @s weapon.mainhand tacz:modern_kinetic_gun[minecraft:custom_data~{GunId:"bf1:m2_2"}] run scoreboard players set #kill_money cs2_econ 600
execute if items entity @s weapon.mainhand tacz:modern_kinetic_gun[minecraft:custom_data~{GunId:"tacz:ai_awp"}] run scoreboard players set #kill_money cs2_econ 100
execute if items entity @s weapon.mainhand tacz:modern_kinetic_gun[minecraft:custom_data~{GunId:"claymore:zerotime"}] run scoreboard players set #kill_money cs2_econ 1500
scoreboard players operation @s cs2_money += #kill_money cs2_econ
tellraw @s [{"text":"击杀奖励 ","color":"green"},{"text":"+$","color":"gold"},{"score":{"name":"#kill_money","objective":"cs2_econ"},"color":"gold"}]
