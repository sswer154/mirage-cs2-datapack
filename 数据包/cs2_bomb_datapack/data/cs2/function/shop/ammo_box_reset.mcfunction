# 仅在 ensure_default_t/ct 确认副手已有弹药盒、且快捷栏/背包还有多余时调用。
# clear 会连副手一起清掉，所以清完必须重新放回副手一个。
clear @s tacz:ammo_box[minecraft:custom_data~{AllTypeCreative:1b}]
item replace entity @s weapon.offhand with tacz:ammo_box[minecraft:custom_data={AllTypeCreative:1b,cs2_drop_clean:1b}]
