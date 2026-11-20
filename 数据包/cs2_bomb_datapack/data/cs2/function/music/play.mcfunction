# 回合与比赛结算的音乐盒接口
# 预设编号存放在 cs2_cfg 的 #music_preset：0=关闭，1..7=对应曲目。
# 按参赛者当前位置播放，避免因距离命令执行点太远而收不到。
# 先停掉上一回合可能还在播放的曲目，避免长曲跨回合叠音。
execute as @a[scores={cs2_inmatch=1}] run function cs2:music/stop

execute if score #music_preset cs2_cfg matches 0 run return 0

# 1 束光似水
execute if score #music_preset cs2_cfg matches 1 as @a[scores={cs2_inmatch=1}] at @s run playsound cs2:music.shuguang master @s
# 2 inhuman_mvp
execute if score #music_preset cs2_cfg matches 2 as @a[scores={cs2_inmatch=1}] at @s run playsound cs2:music.inhuman_mvp master @s
# 3 invasion_mvp
execute if score #music_preset cs2_cfg matches 3 as @a[scores={cs2_inmatch=1}] at @s run playsound cs2:music.invasion_mvp master @s
# 4 umri_15s
execute if score #music_preset cs2_cfg matches 4 as @a[scores={cs2_inmatch=1}] at @s run playsound cs2:music.umri_15s master @s
# 5 on_and_on_60s
execute if score #music_preset cs2_cfg matches 5 as @a[scores={cs2_inmatch=1}] at @s run playsound cs2:music.on_and_on_60s master @s
# 6 reimei_10s
execute if score #music_preset cs2_cfg matches 6 as @a[scores={cs2_inmatch=1}] at @s run playsound cs2:music.reimei_10s master @s
# 7 know_my_name_15s
execute if score #music_preset cs2_cfg matches 7 as @a[scores={cs2_inmatch=1}] at @s run playsound cs2:music.know_my_name_15s master @s
