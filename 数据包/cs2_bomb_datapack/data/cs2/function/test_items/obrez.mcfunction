# 截断莫辛纳甘  $800
scoreboard players set @s cs2_wclass 1
clear @s tacz:modern_kinetic_gun[minecraft:custom_data~{GunId:"tacz:glock_17"}]
clear @s tacz:modern_kinetic_gun[minecraft:custom_data~{GunId:"tacz:m9a4"}]
scoreboard players set @s cs2_secondary 1
give @s tacz:modern_kinetic_gun[minecraft:custom_data={cs2_secondary_gun:1b,GunCurrentAmmoCount:4,GunFireMode:"SEMI",GunId:"bf1:obrez",HasBulletInBarrel:0b}]
