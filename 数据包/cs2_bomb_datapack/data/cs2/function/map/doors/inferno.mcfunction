# 小镇 inferno：双扇红树木门，回合开始恢复关闭状态。
# 先清空上下半门，再按 lower/upper 顺序放回。
fill 658 40 -3557 658 41 -3557 air
setblock 658 40 -3557 minecraft:mangrove_door[facing=north,half=lower,hinge=left,open=false,powered=false]
setblock 658 41 -3557 minecraft:mangrove_door[facing=north,half=upper,hinge=left,open=false,powered=false]
fill 659 40 -3557 659 41 -3557 air
setblock 659 40 -3557 minecraft:mangrove_door[facing=north,half=lower,hinge=right,open=false,powered=false]
setblock 659 41 -3557 minecraft:mangrove_door[facing=north,half=upper,hinge=right,open=false,powered=false]
