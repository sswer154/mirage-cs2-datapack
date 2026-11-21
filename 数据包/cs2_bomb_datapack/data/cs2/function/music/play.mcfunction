# 回合与比赛结算的音乐盒接口
# 预设编号存放在 cs2_cfg 的 #music_preset：0=关闭，1..7=指定曲目，8=轮播。
# 按参赛者和等待加入玩家当前位置播放，避免因距离命令执行点太远而收不到。
# 先停掉上一回合可能还在播放的曲目，避免长曲跨回合叠音。
execute as @a[predicate=cs2:match_audio] run function cs2:music/stop

execute unless score #music_preset cs2_cfg matches 1..8 run return 0
scoreboard players operation #music_track cs2_counts = #music_preset cs2_cfg
execute if score #music_preset cs2_cfg matches 8 run function cs2:music/rotate

# 1 束光似水
execute if score #music_track cs2_counts matches 1 as @a[predicate=cs2:match_audio] at @s run playsound cs2:music.shuguang master @s
# 2 inhuman_mvp
execute if score #music_track cs2_counts matches 2 as @a[predicate=cs2:match_audio] at @s run playsound cs2:music.inhuman_mvp master @s
# 3 invasion_mvp
execute if score #music_track cs2_counts matches 3 as @a[predicate=cs2:match_audio] at @s run playsound cs2:music.invasion_mvp master @s
# 4 umri_15s
execute if score #music_track cs2_counts matches 4 as @a[predicate=cs2:match_audio] at @s run playsound cs2:music.umri_15s master @s
# 5 on_and_on_60s
execute if score #music_track cs2_counts matches 5 as @a[predicate=cs2:match_audio] at @s run playsound cs2:music.on_and_on_60s master @s
# 6 reimei_10s
execute if score #music_track cs2_counts matches 6 as @a[predicate=cs2:match_audio] at @s run playsound cs2:music.reimei_10s master @s
# 7 know_my_name_15s
execute if score #music_track cs2_counts matches 7 as @a[predicate=cs2:match_audio] at @s run playsound cs2:music.know_my_name_15s master @s
