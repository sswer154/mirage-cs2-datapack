# 比赛结束：逐人再显示一次本场 K/D，并列出本场击杀最多者
# 必须在 match/end 清空 cs2_inmatch 之前调用

execute as @a[scores={cs2_inmatch=1}] run tellraw @s [{"text":"本场 K/D  ","color":"gold"},{"score":{"name":"@s","objective":"cs2_kills"},"color":"white"},{"text":" / ","color":"gray"},{"score":{"name":"@s","objective":"cs2_deaths"},"color":"white"}]

scoreboard players set #top_kills cs2_econ 0
execute as @a[scores={cs2_inmatch=1}] if score @s cs2_kills > #top_kills cs2_econ run scoreboard players operation #top_kills cs2_econ = @s cs2_kills

execute if score #top_kills cs2_econ matches 1.. run tellraw @a[scores={cs2_inmatch=1}] [{"text":"本场击杀王：","color":"gold"}]
execute if score #top_kills cs2_econ matches 0 run tellraw @a[scores={cs2_inmatch=1}] [{"text":"本场没有产生击杀。","color":"gray"}]
execute as @a[scores={cs2_inmatch=1}] if score @s cs2_kills = #top_kills cs2_econ if score #top_kills cs2_econ matches 1.. run tellraw @a[scores={cs2_inmatch=1}] [{"text":"- ","color":"gray"},{"selector":"@s","color":"yellow"},{"text":"  ","color":"gray"},{"score":{"name":"@s","objective":"cs2_kills"},"color":"gold"},{"text":" 杀","color":"gray"}]
