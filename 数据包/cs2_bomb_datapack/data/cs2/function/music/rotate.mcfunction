# 每次结算选一首曲目；同场收听者使用同一编号。
execute unless score #music_next cs2_counts matches 1..7 run scoreboard players set #music_next cs2_counts 1
scoreboard players operation #music_track cs2_counts = #music_next cs2_counts
scoreboard players add #music_next cs2_counts 1
