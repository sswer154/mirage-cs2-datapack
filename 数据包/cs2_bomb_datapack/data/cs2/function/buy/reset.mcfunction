# 清空每名玩家的经济/装备选择状态
# buy: 1=ECO/手枪 2=强起 3=长枪 4=狙击
# choice: 0=未选择，其他同 buy
scoreboard players set @a cs2_buy 0
scoreboard players set @a cs2_choice 0
scoreboard players set @a cs2_allow_eco 0
scoreboard players set @a cs2_allow_force 0
scoreboard players set @a cs2_allow_rifle 0
scoreboard players set @a cs2_allow_sniper 0

# 清理旧版队伍级假分数，避免旧函数残留干扰
scoreboard players set #buy_t cs2_buy 0
scoreboard players set #buy_ct cs2_buy 0
scoreboard players set #choice_t cs2_buy 0
scoreboard players set #choice_ct cs2_buy 0
scoreboard players set #allow_eco_t cs2_buy 0
scoreboard players set #allow_eco_ct cs2_buy 0
scoreboard players set #allow_force_t cs2_buy 0
scoreboard players set #allow_force_ct cs2_buy 0
scoreboard players set #allow_rifle_t cs2_buy 0
scoreboard players set #allow_rifle_ct cs2_buy 0
scoreboard players set #allow_sniper_t cs2_buy 0
scoreboard players set #allow_sniper_ct cs2_buy 0