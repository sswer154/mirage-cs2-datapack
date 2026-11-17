advancement revoke @s only cs2:inventory/pickup_p90
execute if entity @s[tag=cs2_shop_buying] run return 0
execute if entity @s[tag=cs2_primary_busy] run return 0
tag @s add cs2_primary_busy
execute if score @s cs2_primary matches 1 run tag @s add cs2_had_primary
execute if score @s cs2_primary matches 0 run scoreboard players set @s cs2_primary 1
execute if entity @s[tag=cs2_had_primary] run clear @s tacz:modern_kinetic_gun[minecraft:custom_data~{GunId:"tacz:p90"}] 1
execute if entity @s[tag=cs2_had_primary] run summon item ^ ^ ^ {Item:{id:"tacz:modern_kinetic_gun",count:1,components:{"minecraft:custom_data":{cs2_primary_gun:1b,AttachmentSCOPE:{},GunCurrentAmmoCount:50,GunFireMode:"AUTO",GunId:"tacz:p90",HasBulletInBarrel:1b}}},PickupDelay:100s}
scoreboard players set @s cs2_primary 1
tag @s remove cs2_had_primary
tag @s remove cs2_primary_busy
advancement revoke @s only cs2:inventory/pickup_p90
