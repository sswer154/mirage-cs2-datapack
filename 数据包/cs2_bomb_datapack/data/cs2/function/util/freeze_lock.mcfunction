# 准备阶段冻结：slowness 255 + resistance 255 + 跳跃锁
# 四个入口（round/freeze、join/freeze_t、join/freeze_ct、player/rejoin_freeze）统一调用，
# 避免以后某个入口漏写导致能走动
effect give @s minecraft:slowness 30 255 true
effect give @s minecraft:resistance 30 255 true
function cs2:util/jump_lock
