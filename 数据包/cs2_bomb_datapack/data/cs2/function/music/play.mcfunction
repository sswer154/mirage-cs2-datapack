# 赛后音乐盒接口
# 预设编号存放在 cs2_cfg 的 #music_preset：0=关闭，1..7=对应曲目。
# 由 match/end 在比赛正常结束后调用；只给本局参赛者播放。
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
