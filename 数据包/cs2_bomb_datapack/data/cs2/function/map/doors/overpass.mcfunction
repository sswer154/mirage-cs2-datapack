# 游乐场 overpass：单扇诡异木门，回合开始恢复关闭状态。
# 先清空上下半门，再按 lower/upper 顺序放回。
fill 575 41 -4584 575 42 -4584 air
setblock 575 41 -4584 minecraft:warped_door[facing=north,half=lower,hinge=right,open=false,powered=false]
setblock 575 42 -4584 minecraft:warped_door[facing=north,half=upper,hinge=right,open=false,powered=false]
