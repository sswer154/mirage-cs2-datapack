# 播报目标：参赛者 + 解说；调用前先清，避免标签残留到大厅玩家
tag @a remove cs2_feed_target
tag @a[scores={cs2_inmatch=1}] add cs2_feed_target
tag @a[team=cs2_cast] add cs2_feed_target
