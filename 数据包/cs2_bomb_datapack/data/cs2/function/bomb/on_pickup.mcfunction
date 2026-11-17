# inventory_changed 触发器：拾取 C4 时调用
advancement revoke @s only cs2:bomb/pickup
execute if score @s cs2_team matches 1 run function cs2:bomb/pickup_t
execute if score @s cs2_team matches 2 run function cs2:bomb/pickup_ct
execute if score @s cs2_team matches 0 run function cs2:bomb/pickup_spec
