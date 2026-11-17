# 删除全部地图配置，并停止当前比赛
function cs2:match/stop
kill @e[type=marker,tag=cs2_spawn_t]
kill @e[type=marker,tag=cs2_spawn_ct]
kill @e[type=marker,tag=cs2_site_a]
kill @e[type=marker,tag=cs2_site_b]
kill @e[type=marker,tag=cs2_lobby]
kill @e[type=text_display,tag=cs2_site_a_text]
kill @e[type=text_display,tag=cs2_site_b_text]
# 多地图：删除所有地图配置与包点文字
kill @e[type=marker,tag=cs2_map_marker]
kill @e[type=text_display,tag=cs2_map_text]
data modify storage cs2:map active set value "未选择"
kill @e[tag=cs2_bomb]
kill @e[type=item_display,tag=cs2_c4_display]
kill @e[type=item,tag=cs2_util_projectile]
kill @e[type=marker,tag=cs2_smoke]
kill @e[type=marker,tag=cs2_fire]
execute as @e[type=item] if data entity @s Item.components."minecraft:custom_data".cs2_bomb run kill @s
tellraw @a {"text":"地图配置和比赛状态已重置。","color":"yellow"}
