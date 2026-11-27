# 默认配发：M9A4 + 零时大剑 + 创造弹药盒；购买状态清零
# 先清掉上一回合遗留的默认手枪，弹药盒由统一逻辑检查。
clear @s tacz:modern_kinetic_gun[minecraft:custom_data~{GunId:"tacz:glock_17"}]
clear @s tacz:modern_kinetic_gun[minecraft:custom_data~{GunId:"tacz:m9a4"}]
# 换边或重复发装时，只清除先前配发的对局近战武器。
clear @s tacz:modern_kinetic_gun[minecraft:custom_data~{GunId:"claymore:zerotime",cs2_drop_clean:1b}]
clear @s tacz:ammo_box[minecraft:custom_data~{AllTypeCreative:1b}]
give @s tacz:modern_kinetic_gun[minecraft:custom_data={cs2_secondary_gun:1b,GunCurrentAmmoCount:17,GunFireMode:"SEMI",GunId:"tacz:m9a4",HasBulletInBarrel:1b}]
give @s tacz:modern_kinetic_gun[minecraft:custom_data={AttachmentEXTENDED_MAG:{components:{"minecraft:custom_data":{AttachmentId:"claymore:soulgem"}},count:1,id:"tacz:attachment"},AttachmentMUZZLE:{components:{"minecraft:custom_data":{AttachmentId:"claymore:heavyblade"}},count:1,id:"tacz:attachment"},GunCurrentAmmoCount:100,GunFireMode:"BURST",GunId:"claymore:zerotime",HasBulletInBarrel:1b,cs2_drop_clean:1b}]
function cs2:shop/ensure_ammo_box
scoreboard players set @s cs2_primary 0
scoreboard players set @s cs2_secondary 0
scoreboard players set @s cs2_armor 0
scoreboard players set @s cs2_kit 0
scoreboard players set @s cs2_planter 0
scoreboard players set @s cs2_wclass 1
