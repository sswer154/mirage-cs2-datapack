# 在玩家脚下设置 A 包点。
kill @e[type=marker,tag=cs2_site_a]
kill @e[type=text_display,tag=cs2_site_a_text]
execute at @s run summon marker ~ ~ ~ {Tags:["cs2_site_a"]}
execute at @s run tp @e[type=marker,tag=cs2_site_a,limit=1] ~ ~ ~ ~ ~
execute at @s run summon text_display ~ ~2.5 ~ {Tags:["cs2_site_a_text"],text:"{\"text\":\"A 包点\",\"color\":\"red\",\"bold\":true}",billboard:"center",see_through:1b,background:0}
# 强制加载包点区块，避免远距离配置点因区块卸载而查不到 marker
execute at @s run cs2chunks retain ~ ~
tellraw @s {"text":"A 包点已设置（半径 7 格）。","color":"green"}
