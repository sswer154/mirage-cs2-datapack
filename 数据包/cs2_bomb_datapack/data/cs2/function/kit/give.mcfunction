# 给 CT 发放拆弹器（只应由 reissue 调用；回合开始由 loadout/kit 发放）
scoreboard players set @s cs2_kit 1
give @s minecraft:shears[minecraft:custom_data={cs2_kit:1b},minecraft:custom_model_data=721003,minecraft:item_name='{"text":"拆弹器","color":"aqua"}',minecraft:enchantment_glint_override=1b]
title @s actionbar {"text":"获得拆弹器","color":"aqua"}
