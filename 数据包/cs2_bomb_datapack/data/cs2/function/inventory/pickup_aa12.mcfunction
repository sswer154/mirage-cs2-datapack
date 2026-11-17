advancement revoke @s only cs2:inventory/pickup_aa12
execute if entity @s[tag=cs2_shop_buying] run return 0
execute if entity @s[tag=cs2_primary_busy] run return 0
tag @s add cs2_primary_busy
execute if score @s cs2_primary matches 1 run tag @s add cs2_had_primary
execute if score @s cs2_primary matches 0 run scoreboard players set @s cs2_primary 1
execute if entity @s[tag=cs2_had_primary] run clear @s tacz:modern_kinetic_gun[minecraft:custom_data~{GunId:"tacz:aa12"}] 1
execute if entity @s[tag=cs2_had_primary] run summon item ^ ^ ^ {Item:{id:"tacz:modern_kinetic_gun",count:1,components:{"minecraft:custom_data":{cs2_primary_gun:1b,AttachmentEXTENDED_MAG:{components:{"minecraft:custom_data":{AttachmentId:"tacz:extended_mag_1"}},count:1,id:"tacz:attachment"},AttachmentGRIP:{components:{"minecraft:custom_data":{AttachmentId:"tacz:grip_rk1_b25u"}},count:1,id:"tacz:attachment"},AttachmentMUZZLE:{components:{"minecraft:custom_data":{AttachmentId:"tacz:muzzle_brake_mastiff_sg"}},count:1,id:"tacz:attachment"},AttachmentSCOPE:{components:{"minecraft:custom_data":{AttachmentId:"tacz:sight_exp3"}},count:1,id:"tacz:attachment"},GunCurrentAmmoCount:13,GunFireMode:"AUTO",GunId:"tacz:aa12",HasBulletInBarrel:1b}}},PickupDelay:100s}
scoreboard players set @s cs2_primary 1
tag @s remove cs2_had_primary
tag @s remove cs2_primary_busy
advancement revoke @s only cs2:inventory/pickup_aa12
