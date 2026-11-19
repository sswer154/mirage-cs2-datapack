# CT 获胜：拆包 3500，其余 3250
execute if score #reason cs2_counts matches 4 run cs2econ team 2 3500
execute unless score #reason cs2_counts matches 4 run cs2econ team 2 3250
