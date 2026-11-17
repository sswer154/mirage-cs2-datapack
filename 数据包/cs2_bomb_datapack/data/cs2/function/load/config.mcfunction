# 只在第一次安装时写默认配置；之后 reload 不会覆盖你改过的值。
execute unless score #cfg_ready cs2_const matches 1 run function cs2:config/defaults

# 旧存档迁移：准备时间统一改为 25 秒；只迁移一次，之后仍可在 cs2_cfg 里改
execute unless score #freeze25 cs2_const matches 1 run scoreboard players set freeze cs2_cfg 25
execute unless score #freeze25 cs2_const matches 1 run scoreboard players set #freeze25 cs2_const 1
