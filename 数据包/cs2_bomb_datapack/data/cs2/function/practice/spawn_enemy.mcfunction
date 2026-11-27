# 训练模式：在面前召唤无 AI、全套无附魔下界合金甲僵尸
scoreboard players reset @s cs2_train_spawn
scoreboard players enable @s cs2_train_spawn
execute unless score #solo cs2_counts matches 1..2 run tellraw @s {"text":"只有训练模式可以召唤训练敌人。","color":"red"}
execute unless score #solo cs2_counts matches 1..2 run return 0
execute unless score #state cs2_state matches 2..4 run tellraw @s {"text":"只能在准备/回合进行/炸弹阶段召唤训练敌人。","color":"red"}
execute unless score #state cs2_state matches 2..4 run return 0
execute at @s run summon minecraft:zombie ^ ^ ^3 {NoAI:1b,PersistenceRequired:1b,DeathLootTable:"cs2:training_dummy",CanPickUpLoot:0b,ArmorDropChances:[0.0f,0.0f,0.0f,0.0f],HandDropChances:[0.0f,0.0f],Tags:["cs2_training_dummy"],CustomName:'{"text":"训练靶","color":"red"}',CustomNameVisible:1b,ArmorItems:[{id:"minecraft:netherite_boots",count:1},{id:"minecraft:netherite_leggings",count:1},{id:"minecraft:netherite_chestplate",count:1},{id:"minecraft:netherite_helmet",count:1}]}
tellraw @s {"text":"已召唤训练敌人。结束训练时会自动清除。","color":"green"}
