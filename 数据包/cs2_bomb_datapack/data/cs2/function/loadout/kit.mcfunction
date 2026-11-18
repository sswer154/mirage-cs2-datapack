# 拆弹器只作为标记，不发放物品；仅对 cs2_kit=1 的 CT 有意义
execute if score @s cs2_kit matches 1 run title @s actionbar {"text":"拆弹器已就绪","color":"aqua"}
