# 阵营防御：只对本阵营玩家生效，避免被误调用时发错阵营的枪
execute unless score @s cs2_team matches 1 run return 0

# 兜底：准备阶段检查默认手枪/弹药盒是否真的在背包里。
# 正常路径由 shop/default_t 发放；死亡发生在 freeze/回合结束等阶段时，
# round_reset 可能把玩家当成“上局存活”而跳过默认装备，这里补发。
execute if score @s cs2_secondary matches 0 unless items entity @s inventory.* tacz:modern_kinetic_gun[minecraft:custom_data~{GunId:"tacz:glock_17"}] unless items entity @s hotbar.* tacz:modern_kinetic_gun[minecraft:custom_data~{GunId:"tacz:glock_17"}] run give @s tacz:modern_kinetic_gun[minecraft:custom_data={GunCurrentAmmoCount:17,GunFireMode:"SEMI",GunId:"tacz:glock_17",HasBulletInBarrel:1b}]
# 弹药盒只保留一个，并固定放回副手（避免玩家挪到背包后又被补发一个）
# 副手缺失时：先清掉散落的，再补一个；正常每 tick 不重建。
execute unless items entity @s weapon.offhand tacz:ammo_box[minecraft:custom_data~{AllTypeCreative:1b}] run clear @s tacz:ammo_box[minecraft:custom_data~{AllTypeCreative:1b}]
execute unless items entity @s weapon.offhand tacz:ammo_box[minecraft:custom_data~{AllTypeCreative:1b}] run item replace entity @s weapon.offhand with tacz:ammo_box[minecraft:custom_data={AllTypeCreative:1b,cs2_drop_clean:1b}]
# 副手已有但快捷栏/背包里有多余：只在这种情况重置一次。
execute if items entity @s weapon.offhand tacz:ammo_box[minecraft:custom_data~{AllTypeCreative:1b}] if items entity @s hotbar.* tacz:ammo_box[minecraft:custom_data~{AllTypeCreative:1b}] run function cs2:shop/ammo_box_reset
execute if items entity @s weapon.offhand tacz:ammo_box[minecraft:custom_data~{AllTypeCreative:1b}] if items entity @s inventory.* tacz:ammo_box[minecraft:custom_data~{AllTypeCreative:1b}] run function cs2:shop/ammo_box_reset
