# T 获胜：爆炸 3500，其余 3250
execute if score #reason cs2_counts matches 3 run cs2econ team 1 3500
execute unless score #reason cs2_counts matches 3 run cs2econ team 1 3250
