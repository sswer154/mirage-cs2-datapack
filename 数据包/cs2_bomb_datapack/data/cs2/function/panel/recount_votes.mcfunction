execute store result score #vote_total cs2_counts if entity @a[scores={cs2_inmatch=1}]
execute store result score #vote_restart_yes cs2_counts if entity @a[scores={cs2_inmatch=1,cs2_voted_restart=1}]
execute store result score #vote_end_yes cs2_counts if entity @a[scores={cs2_inmatch=1,cs2_voted_end=1}]
