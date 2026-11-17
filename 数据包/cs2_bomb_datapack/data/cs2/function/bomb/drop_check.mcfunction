# 每 tick：给掉落物打 tag；检查携带者是否还拿着 C4；处理掉落倒计时
execute as @e[type=item] if data entity @s Item.components."minecraft:custom_data".cs2_bomb run tag @s add cs2_bomb_item

execute if score #bomb cs2_bomb_state matches 1 as @a[scores={cs2_carrier=1}] unless items entity @s inventory.* minecraft:nautilus_shell[minecraft:custom_data={cs2_bomb:1b}] unless items entity @s hotbar.* minecraft:nautilus_shell[minecraft:custom_data={cs2_bomb:1b}] unless items entity @s weapon.mainhand minecraft:nautilus_shell[minecraft:custom_data={cs2_bomb:1b}] unless items entity @s weapon.offhand minecraft:nautilus_shell[minecraft:custom_data={cs2_bomb:1b}] run function cs2:bomb/carrier_lost

execute if score #bomb cs2_bomb_state matches 2 run function cs2:bomb/drop_tick

# 携带者离线：@a 里找不到 cs2_carrier=1 的人，按已丢失处理，5 秒后补发
execute if score #bomb cs2_bomb_state matches 1 unless entity @a[scores={cs2_carrier=1}] run scoreboard players set #bomb cs2_bomb_state 2
execute if score #bomb cs2_bomb_state matches 1 unless entity @a[scores={cs2_carrier=1}] run scoreboard players set #drop_timer cs2_counts 100
