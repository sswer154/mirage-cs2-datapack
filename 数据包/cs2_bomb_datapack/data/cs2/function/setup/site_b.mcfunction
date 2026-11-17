# 在玩家脚下设置 B 包点。
kill @e[type=marker,tag=cs2_site_b]
kill @e[type=text_display,tag=cs2_site_b_text]
execute at @s run summon marker ~ ~ ~ {Tags:["cs2_site_b"]}
execute at @s run tp @e[type=marker,tag=cs2_site_b,limit=1] ~ ~ ~ ~ ~
execute at @s run summon text_display ~ ~2.5 ~ {Tags:["cs2_site_b_text"],text:"{\"text\":\"B 包点\",\"color\":\"red\",\"bold\":true}",billboard:"center",see_through:1b,background:0}
# 强制加载包点区块，避免远距离配置点因区块卸载而查不到 marker
execute at @s run forceload add ~ ~
tellraw @s {"text":"B 包点已设置（半径 10 格）。","color":"green"}
