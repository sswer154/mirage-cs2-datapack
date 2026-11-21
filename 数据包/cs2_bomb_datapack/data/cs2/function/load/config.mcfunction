# 只在第一次安装时写默认配置；之后 reload 不会覆盖你改过的值。
execute unless score #cfg_ready cs2_const matches 1 run function cs2:config/defaults

# 旧存档迁移：准备时间统一改为 25 秒；只迁移一次，之后仍可在 cs2_cfg 里改
execute unless score #freeze25 cs2_const matches 1 run scoreboard players set freeze cs2_cfg 25
execute unless score #freeze25 cs2_const matches 1 run scoreboard players set #freeze25 cs2_const 1

# 迁移旧的第 1 首默认预设；之后可以手动设回 1。保留 0 和其他手动预设。
execute unless score #music_preset cs2_cfg = #music_preset cs2_cfg run scoreboard players set #music_preset cs2_cfg 8
execute unless score #music_rotate cs2_const matches 1 if score #music_preset cs2_cfg matches 1 run scoreboard players set #music_preset cs2_cfg 8
scoreboard players set #music_rotate cs2_const 1
