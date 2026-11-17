# 给玩家打上阵营 tag，供击杀奖励 advancement 判断受害者阵营
tag @s remove cs2_tag_t
tag @s remove cs2_tag_ct
execute if score @s cs2_team matches 1 run tag @s add cs2_tag_t
execute if score @s cs2_team matches 2 run tag @s add cs2_tag_ct
