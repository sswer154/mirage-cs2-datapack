# 给随机一名存活且没有拆弹器的 CT 重新发放
execute as @r[team=cs2_ct,scores={cs2_alive=1,cs2_kit=0}] run function cs2:kit/give
