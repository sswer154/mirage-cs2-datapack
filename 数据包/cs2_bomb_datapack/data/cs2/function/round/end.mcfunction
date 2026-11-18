scoreboard players set #state cs2_state 5
# 回滚：死亡不掉落模式
gamerule keepInventory true
# 回合结束展示期间关闭 PvP，下一回合 live 再恢复
function cs2:pvp/round_end
scoreboard players operation #timer cs2_timer = round_end cs2_cfg
scoreboard players operation #timer cs2_timer *= #20 cs2_const
bossbar set cs2:round visible false
bossbar set cs2:bomb visible false

# 经济结算必须早于 #bomb 清零，结算里要用 #bomb=3 判断“安包后输”
function cs2:econ/round_end
function cs2:stats/round_highlights

gamemode adventure @a[scores={cs2_inmatch=1,cs2_team=1}]
gamemode adventure @a[scores={cs2_inmatch=1,cs2_team=2}]
effect clear @a[scores={cs2_inmatch=1,cs2_team=1}] minecraft:slowness
effect clear @a[scores={cs2_inmatch=1,cs2_team=1}] minecraft:resistance
effect clear @a[scores={cs2_inmatch=1,cs2_team=2}] minecraft:slowness
effect clear @a[scores={cs2_inmatch=1,cs2_team=2}] minecraft:resistance

kill @e[tag=cs2_bomb]
kill @e[type=item_display,tag=cs2_c4_display]
kill @e[type=armor_stand,tag=cs2_c4_hit]
execute as @e[type=item] if data entity @s Item.components."minecraft:custom_data".cs2_bomb run kill @s
scoreboard players set #bomb cs2_bomb_state 0
scoreboard players set #bomb_timer cs2_bomb_timer 0
scoreboard players set #blink cs2_blink 0
scoreboard players set #blink_on cs2_blink 0

title @a[scores={cs2_inmatch=1}] times 10 60 10
execute if score #winner cs2_counts matches 1 run title @a[scores={cs2_inmatch=1,cs2_hud=1}] title {"text":"T 获胜","color":"red","bold":true}
execute if score #winner cs2_counts matches 2 run title @a[scores={cs2_inmatch=1,cs2_hud=1}] title {"text":"CT 获胜","color":"blue","bold":true}
execute if score #winner cs2_counts matches 0 run title @a[scores={cs2_inmatch=1,cs2_hud=1}] title {"text":"回合结束","color":"gray","bold":true}

execute if score #reason cs2_counts matches 1 run title @a[scores={cs2_inmatch=1,cs2_hud=1}] subtitle {"text":"T 被全部消灭","color":"gray"}
execute if score #reason cs2_counts matches 2 run title @a[scores={cs2_inmatch=1,cs2_hud=1}] subtitle {"text":"CT 被全部消灭","color":"gray"}
execute if score #reason cs2_counts matches 3 run title @a[scores={cs2_inmatch=1,cs2_hud=1}] subtitle {"text":"炸弹爆炸","color":"red"}
execute if score #reason cs2_counts matches 4 run title @a[scores={cs2_inmatch=1,cs2_hud=1}] subtitle {"text":"炸弹被拆除","color":"aqua"}
execute if score #reason cs2_counts matches 5 run title @a[scores={cs2_inmatch=1,cs2_hud=1}] subtitle {"text":"时间耗尽","color":"gray"}
execute if score #reason cs2_counts matches 6 run title @a[scores={cs2_inmatch=1,cs2_hud=1}] subtitle {"text":"炸弹安放后 CT 被全部消灭","color":"gray"}

tellraw @a[scores={cs2_inmatch=1}] [{"text":"回合结束  比分  T ","color":"gold"},{"score":{"name":"#t","objective":"cs2_score_t"}},{"text":" : ","color":"white"},{"score":{"name":"#ct","objective":"cs2_score_ct"}},{"text":" CT","color":"gold"}]
playsound minecraft:ui.toast.challenge_complete master @a[scores={cs2_inmatch=1}]
