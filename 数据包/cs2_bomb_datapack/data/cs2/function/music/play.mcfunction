# 赛后音乐盒接口
# 预设编号存放在 cs2_cfg 的 #music_preset：0=关闭，1..=对应曲目。
# 当前没有配置任何曲目，因此这个函数暂时只保留接口。
# 添加曲目时，在下方按编号加入 playsound，例如：
# execute if score #music_preset cs2_cfg matches 1 run playsound cs2:music.victory master @a
# 如果要让音乐只给参赛者，把 @a 改成 @a[scores={cs2_inmatch=1}]。
execute if score #music_preset cs2_cfg matches 0 run return 0
