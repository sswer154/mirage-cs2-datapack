# 每回合结束音乐盒接口
# 预设编号存放在 cs2_cfg 的 #music_preset：0=关闭，1..7=对应曲目。
# 由 round/end 在每个回合结束后调用；只给本局参赛者播放。
# 先停掉上一回合可能还在播放的曲目，避免长曲跨回合叠音。
stopsound @a[scores={cs2_inmatch=1}] master cs2:music.shuguang
stopsound @a[scores={cs2_inmatch=1}] master cs2:music.inhuman_mvp
stopsound @a[scores={cs2_inmatch=1}] master cs2:music.invasion_mvp
stopsound @a[scores={cs2_inmatch=1}] master cs2:music.umri_15s
stopsound @a[scores={cs2_inmatch=1}] master cs2:music.on_and_on_60s
stopsound @a[scores={cs2_inmatch=1}] master cs2:music.reimei_10s
stopsound @a[scores={cs2_inmatch=1}] master cs2:music.know_my_name_15s

execute if score #music_preset cs2_cfg matches 0 run return 0

# 1 束光似水
execute if score #music_preset cs2_cfg matches 1 run playsound cs2:music.shuguang master @a[scores={cs2_inmatch=1}]
# 2 inhuman_mvp
execute if score #music_preset cs2_cfg matches 2 run playsound cs2:music.inhuman_mvp master @a[scores={cs2_inmatch=1}]
# 3 invasion_mvp
execute if score #music_preset cs2_cfg matches 3 run playsound cs2:music.invasion_mvp master @a[scores={cs2_inmatch=1}]
# 4 umri_15s
execute if score #music_preset cs2_cfg matches 4 run playsound cs2:music.umri_15s master @a[scores={cs2_inmatch=1}]
# 5 on_and_on_60s
execute if score #music_preset cs2_cfg matches 5 run playsound cs2:music.on_and_on_60s master @a[scores={cs2_inmatch=1}]
# 6 reimei_10s
execute if score #music_preset cs2_cfg matches 6 run playsound cs2:music.reimei_10s master @a[scores={cs2_inmatch=1}]
# 7 know_my_name_15s
execute if score #music_preset cs2_cfg matches 7 run playsound cs2:music.know_my_name_15s master @a[scores={cs2_inmatch=1}]
