# 比赛结束：逐人再显示一次本场 K/D，并列出本场击杀最多者
# 必须在 match/end 清空 cs2_inmatch 之前调用

execute as @a[scores={cs2_inmatch=1}] run tellraw @s [{"text":"本场 K/D  ","color":"gold"},{"score":{"name":"@s","objective":"cs2_kills"},"color":"white"},{"text":" / ","color":"gray"},{"score":{"name":"@s","objective":"cs2_deaths"},"color":"white"}]

# 计分板也保留掉线参赛者，用服务端结算读取完整名单。
cs2stats announce_top_kills
