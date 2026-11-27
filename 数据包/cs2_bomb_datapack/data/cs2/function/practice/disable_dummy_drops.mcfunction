# 兼容已经召唤出来的训练靶。
execute as @e[type=minecraft:zombie,tag=cs2_training_dummy] run data merge entity @s {DeathLootTable:"cs2:training_dummy",CanPickUpLoot:0b,ArmorDropChances:[0.0f,0.0f,0.0f,0.0f],HandDropChances:[0.0f,0.0f]}
