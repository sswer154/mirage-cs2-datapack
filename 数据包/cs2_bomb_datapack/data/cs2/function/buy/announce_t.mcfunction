execute if score @s cs2_buy matches 1 run tellraw @a[team=cs2_t] [{"selector":"@s"},{"text":" → ECO（手枪 + 无甲）","color":"green"}]
execute if score @s cs2_buy matches 2 run tellraw @a[team=cs2_t] [{"selector":"@s"},{"text":" → 强起（冲锋枪 + 无甲）","color":"yellow"}]
execute if score @s cs2_buy matches 3 run tellraw @a[team=cs2_t] [{"selector":"@s"},{"text":" → 长枪（步枪 + 铁甲）","color":"aqua"}]
execute if score @s cs2_buy matches 4 run tellraw @a[team=cs2_t] [{"selector":"@s"},{"text":" → 狙击（狙击枪 + 铁甲）","color":"light_purple"}]