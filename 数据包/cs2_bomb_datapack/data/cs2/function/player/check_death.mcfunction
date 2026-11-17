# deathCount 变化检测；每次死亡只处理一次
execute unless score @s cs2_deaths = @s cs2_prev_deaths run function cs2:player/death_check
