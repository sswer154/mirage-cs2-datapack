# 核子 nuke：双层木门，回合开始恢复关闭状态。
# 先清空上下半门，再按 lower/upper 顺序放回；直接 setblock 打开状态的门可能被旧门状态干扰。
fill 491 61 -4305 491 62 -4305 air
setblock 491 61 -4305 minecraft:birch_door[facing=north,half=lower,hinge=left,open=false,powered=false]
setblock 491 62 -4305 minecraft:birch_door[facing=north,half=upper,hinge=left,open=false,powered=false]
fill 492 61 -4305 492 62 -4305 air
setblock 492 61 -4305 minecraft:birch_door[facing=north,half=lower,hinge=right,open=false,powered=false]
setblock 492 62 -4305 minecraft:birch_door[facing=north,half=upper,hinge=right,open=false,powered=false]

fill 493 50 -4303 493 51 -4303 air
setblock 493 50 -4303 minecraft:birch_door[facing=south,half=lower,hinge=left,open=false,powered=false]
setblock 493 51 -4303 minecraft:birch_door[facing=south,half=upper,hinge=left,open=false,powered=false]

fill 501 50 -4283 501 51 -4283 air
setblock 501 50 -4283 minecraft:birch_door[facing=south,half=lower,hinge=right,open=false,powered=false]
setblock 501 51 -4283 minecraft:birch_door[facing=south,half=upper,hinge=right,open=false,powered=false]
fill 502 50 -4283 502 51 -4283 air
setblock 502 50 -4283 minecraft:birch_door[facing=south,half=lower,hinge=left,open=false,powered=false]
setblock 502 51 -4283 minecraft:birch_door[facing=south,half=upper,hinge=left,open=false,powered=false]
