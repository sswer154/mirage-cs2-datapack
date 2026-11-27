# 阵营防御：只对本阵营玩家生效，避免被误调用时发错阵营的枪
execute unless score @s cs2_team matches 2 run return 0

# 兜底：准备阶段检查默认手枪/弹药盒是否真的在背包里。
# 正常路径由 shop/default_ct 发放；死亡发生在 freeze/回合结束等阶段时，
# round_reset 可能把玩家当成“上局存活”而跳过默认装备，这里补发。
execute if score @s cs2_secondary matches 0 unless items entity @s inventory.* tacz:modern_kinetic_gun[minecraft:custom_data~{GunId:"tacz:m9a4"}] unless items entity @s hotbar.* tacz:modern_kinetic_gun[minecraft:custom_data~{GunId:"tacz:m9a4"}] unless items entity @s weapon.offhand tacz:modern_kinetic_gun[minecraft:custom_data~{GunId:"tacz:m9a4"}] run give @s tacz:modern_kinetic_gun[minecraft:custom_data={GunCurrentAmmoCount:17,GunFireMode:"SEMI",GunId:"tacz:m9a4",HasBulletInBarrel:1b}]
function cs2:shop/ensure_ammo_box
