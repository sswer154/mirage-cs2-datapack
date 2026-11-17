# 开局播报：只把本队每名玩家的装备发给本队，敌人看不到
tellraw @a[team=cs2_t] {"text":"──── 本队装备 ────","color":"red"}
execute as @a[team=cs2_t] run function cs2:buy/announce_t
tellraw @a[team=cs2_ct] {"text":"──── 本队装备 ────","color":"blue"}
execute as @a[team=cs2_ct] run function cs2:buy/announce_ct