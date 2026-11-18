# CS2 爆破模式 · Minecraft 1.21.1 数据包

一个带**CS2 风格金钱/商店系统**的 CS2 爆破模式数据包：MR12、每回合商店钻石购买、800 初始资金、连败奖励、击杀奖金、TACZ 武器/护甲购买、随机 T 携带 C4、安放/拆除炸弹、半场换边、加时、阵亡观战、K/D 统计与击杀播报、HUD。

- 适用版本：Minecraft **1.21.1**（数据包 `pack_format 48`）+ **TaCZ 1.21.1 NeoForge 移植版**
- 前置：安装 TaCZ 1.21.1 移植版；商店里所有枪械/弹药盒都使用 TACZ 物品。
- 经济：每名玩家独立资金；开局默认手枪 + 零时大剑 + 创造弹药盒，其他武器/护甲/拆弹器在准备阶段用商店钻石购买。
- TACZ：武器已写入 `tacz:modern_kinetic_gun` 的 1.21.1 数据组件格式；如果移植版的物品 ID/组件结构不同，改 `gen_shop.py` 后重新生成 `shop/` 即可。
- 资源包：可选，用于把 C4 / 拆弹器显示成自定义贴图；不装也能正常玩。

---

## 1. 特性

- **MR12**：先到 13 分获胜；第 12 回合结束换边；12:12 进入加时，加时先到 16，15:15 后突然死亡。
- **CS2 经济**：每人初始 $800；每回合准备阶段用商店钻石购买武器/护甲/拆弹器；资金、连败奖励、击杀奖金按 CS2 规则结算。
- **商店物品**：手枪、冲锋枪、步枪、霰弹枪、射手步枪、机枪、狙击枪、投掷物；部分武器 T/CT 专属；护甲分无甲/半甲 $650/全甲 $1000，拆弹器 $400。
- **医疗用品**：野战手术包 $500；读条 8 秒、回复 6 点生命并解除负面效果，每人每回合 1 份。
- **C4**：每回合随机一名 T 获得 C4；在 A/B 包点长按右键 3.2 秒安放；炸弹 40 秒后爆炸。
- **拆包**：CT 在炸弹 3 格内保持潜行 10 秒；持有拆弹器时 5 秒。松开潜行或离开范围会重置进度。
- **速度 II + 夜视**：比赛开始后所有参赛者获得无限时长的速度 II 和夜视；回合间保留，比赛结束/离场时清除。
- **回合胜负**：T 全灭、CT 全灭、炸弹爆炸、拆包成功、时间耗尽。
- **观战**：阵亡后保留原 T/CT 队伍，自动进入旁观视角并锁定本队仍存活的队友；本队没有存活队友时视角停在阵亡位置。下一回合自动回到本方出生点；中途重连按当前阶段处理。
- **每玩家独立经济**：每名玩家自己的资金、购买状态和武器，队友的购买不影响你；商店界面只对你显示。
- **HUD**：回合、比分、存活、剩余时间/炸弹倒计时全部整合到动作栏；BossBar 计时、回合/比赛标题；每名玩家可用 `/trigger cs2_hud_toggle` 独立开关。无侧边栏。
- **K/D 与击杀播报**：每场比赛开始时重置个人击杀/死亡数；每小局准备阶段聊天栏显示一次自己的本局 K/D；击杀播报与击杀归属由服务端模组 `cs2grenade` 的 `LivingDeathEvent` 精确处理（不发给未参赛玩家），并显示击杀武器/投掷物。友伤击杀会标记 `[友伤]`，并计入击杀/死亡。
- **地图配置**：用游戏内命令在脚下记录 T/CT 出生点、A/B 包点和大厅，无需改文件。
- **不破坏地形**：炸弹爆炸只对实体造成伤害，不炸方块。
- **不自动拉人/清包**：上线不会自动加入队伍、不会传送到大厅、不会清空背包；只有点击加入按钮/执行加入命令才会进入队伍。比赛正常结束只把参赛者传回大厅，未参赛人员保持原地；也不会自动清包；`/function cs2:match/stop` 只让参赛者离队，不传送、不清包。
- **准备阶段开始时清地面物品**：每回合准备阶段开始时清除地面掉落物，避免上一局残留的基础手枪/弹药堆积。

---

## 2. 安装

### 2.1 数据包

把 `cs2_bomb_datapack` 文件夹（或 `cs2_bomb_datapack.zip`）放进世界的 `datapacks` 目录：

```
<存档或服务器目录>/world/datapacks/cs2_bomb_datapack/
或
<存档或服务器目录>/world/datapacks/cs2_bomb_datapack.zip
```

注意：

- **不要把 `CS2Bomb_MC1.21.1_bundle.zip` 直接放进 `datapacks`**。它是“数据包 + 资源包 + README”的合集，不是数据包本身；直接放会导致找不到 `cs2:` 函数。
- 如果解压 `cs2_bomb_datapack.zip`，必须保证 `pack.mcmeta` 位于 `world/datapacks/cs2_bomb_datapack/pack.mcmeta`，不能把 `pack.mcmeta` 和 `data` 直接散落在 `world/datapacks/` 下。
- 数据包必须放在**当前世界**的 `datapacks` 目录；服务器默认是 `world/datapacks`，单人存档是 `.minecraft/saves/<存档名>/datapacks`。

然后执行：

```mcfunction
/reload
/datapack list
```

`/datapack list` 应显示 `file/cs2_bomb_datapack.zip` 或 `file/cs2_bomb_datapack`。首次加载会创建计分板、队伍、BossBar 和 HUD。若看到 `cs2_const already exists` 之类的提示，是 `/reload` 时重复创建计分板的正常现象，不影响使用。

检查是否加载：

```mcfunction
/function cs2:setup/help
```

> `setup/help` 和 `setup/status` 使用 `tellraw @s`，请在游戏内由玩家执行，不要从控制台执行。

### 2.2 资源包（可选）

把 `cs2_bomb_resourcepack` 文件夹（或 zip）放到客户端的 `resourcepacks` 并启用：

- C4 物品与地面模型使用 `custom_model_data 721001/721002`
- 拆弹器使用 `custom_model_data 721003`

不启用资源包时，C4 显示为海螺壳、拆弹器显示为剪刀，功能不受影响。

服务器可选的自动下发（把 zip 放到可访问的 HTTP 地址）：

```properties
resource-pack=https://你的地址/cs2_bomb_resourcepack.zip
resource-pack-sha1=<zip 的 sha1>
require-resource-pack=false
```

---

## 3. 地图配置

先建好大厅、T 出生区、CT 出生区和 A/B 包点。然后由管理员在游戏内站到对应位置执行：

| 位置 | 命令 |
|---|---|
| T 出生点 | `/function cs2:setup/spawn_t` |
| CT 出生点 | `/function cs2:setup/spawn_ct` |
| A 包点 | `/function cs2:setup/site_a` |
| B 包点 | `/function cs2:setup/site_b` |
| 大厅/集合点（热身用） | `/function cs2:setup/lobby` |

说明：

- 命令会在你**脚下**生成一个 marker，并记录你当前**朝向**；玩家出生时会复制该位置和朝向。
- 包点判定半径为 **10 格**（固定值，见“已知限制”）。
- 包点会额外生成一个悬浮的 `A 包点` / `B 包点` 文字。
- 设置点时会自动 `forceload` 该区块，并把坐标写入 `cs2:map` 存储，避免远距离配置点所在区块卸载后，`match/start` 或 `map/switch` 找不到 marker。
- 开赛时还会对五个关键点再次 `forceload`，确保比赛结束、换边时大厅/出生点 marker 仍在加载区块内；如果仍找不到大厅点，重新执行一次 `cs2:setup/lobby`。
- 如果升级前已经设置过出生点/包点，请先执行一次 `map/adopt` 把旧配置收编成命名地图（会补存坐标并 forceload）；或者站到对应位置重新执行一次设置命令。
- 不要用 `/kill @e[type=marker]` 清实体，否则会删掉配置点。

配置完成后检查：

```mcfunction
/function cs2:setup/check
/function cs2:setup/status
```

`check` 会检查五个点是否齐全；`status` 会显示每个点的坐标。

### 3.1 多地图与一键切换

每个地图单独存一套点，地图名建议只用小写字母、数字和下划线，例如 `dust`、`mirage`、`inferno`。

**建图**：站到对应位置，把 `dust` 换成你的地图名：

```mcfunction
/function cs2:setup/map/spawn_t {map:"dust"}
/function cs2:setup/map/spawn_ct {map:"dust"}
/function cs2:setup/map/site_a {map:"dust"}
/function cs2:setup/map/site_b {map:"dust"}
/function cs2:setup/map/lobby {map:"dust"}
```

五个点可以来自不同次执行，最后用 `map/check` 确认：

```mcfunction
/function cs2:map/check {map:"dust"}
```

**切换**（只能在未开始比赛时，state=0）：

```mcfunction
/function cs2:map/switch {map:"dust"}
/function cs2:map/status
```

切换会把该地图的五个点启用为当前配置，并隐藏其他地图的 A/B 文字；当前地图名保存在 `cs2:map` 的 `active` 字段。

**把旧配置收编成地图**：如果你已经用原来的 `cs2:setup/spawn_t` 等命令建好一套点，先执行：

```mcfunction
/function cs2:map/adopt {map:"旧地图名"}
```

它会把当前启用的五个点标记成该地图名，同时把五个点的坐标写入 `cs2:map` 存储并补 `forceload`；之后即使区块卸载，`map/switch` 也能先按存储坐标加载，再切换。

**命令方块一键切换**：命令方块设为“需要红石”，旁边放按钮，命令写：

```mcfunction
execute as @p run function cs2:map/switch {map:"dust"}
```

每一张地图都做一个按钮即可。建图按钮同理，用 `execute as @p run function cs2:setup/map/spawn_t {map:"dust"}`。

注意：

- `map/switch` 不能在比赛中切换；先 `/function cs2:match/stop`。
- `setup/reset` 会删除**所有地图**的配置点和文字，不只当前地图。
- 新建地图的 `setup/map/...` 会自动保存五个点坐标并 `forceload`；`map/adopt` 也会为旧配置补存坐标。`map/switch` / `map/check` 在检查 marker 前会先按 `cs2:map` 中保存的坐标执行 `forceload`，所以正常情况下目标区块卸载也能自动加载。
- 只有“在坐标存储机制加入前建好、又从未执行过 `map/adopt`”的地图，才会没有坐标记录；如果它的区块已经完全卸载，`@e` 找不到 marker，数据包也无法仅凭地图名推断坐标。请先站到该地图附近让区块加载，执行一次 `map/adopt`（或重新执行该地图的 `setup/map/...`）。之后即可正常自动加载。
- 已加载但未 forceload 的零散地图点，可用 `/function cs2:map/forceload_all` 一次性补加载。

### 3.2 可操作门重置

`nuke`、`overpass`、`inferno` 三张地图在回合开始时会把配置过的门恢复为关闭状态，门类型、朝向和铰链关系不变：

```mcfunction
# 内置地图（按 cs2:map 的 active 字段自动分派）
/function cs2:map/doors/reset
```

三个地图的函数分别是 `map/doors/nuke`、`map/doors/overpass`、`map/doors/inferno`，内部按上下半门分别 `setblock`。新增地图时复制一份函数并把坐标改成自己的门即可；`reset` 只对已配置的地图生效，未知地图不会误动。

需要重来：

```mcfunction
/function cs2:setup/reset
```

它会删除所有配置点、炸弹实体，并停止当前比赛。

---

## 4. 开始游戏

数据包有 **T / CT 两个参赛队伍**，以及一个可选的 **解说队伍 `cs2_cast`**；没有旁观队伍。未加入的玩家不属于任何队伍，上线时不会被自动传送、不会自动清空背包，也不会被切模式。

加入/离开：

```mcfunction
/trigger cs2_join_t
/trigger cs2_join_ct
/trigger cs2_join_cast
/trigger cs2_leave
```

**解说队伍：**

- 只能手动加入，不会在开赛时自动拉人。
- 加入后为旁观模式，`cs2_team=3`、`cs2_inmatch=0`，不占 T/CT 人数，不参与回合、炸弹、经济、胜负和 K/D 计算。
- 只额外接收模组发出的精确击杀播报，不接收选手的回合 K/D 提示。
- 只有当前不是参赛者时才能转解说；正在比赛中会拒绝，避免从 T/CT 拉人导致人数变化。
- 离开用 `/trigger cs2_leave`，恢复为未加入队伍的冒险模式。
- 命令方块示例：`execute as @p run function cs2:join/cast`。

- 准备阶段/热身/回合结束/半场休息时可以加入。
- 回合进行中（live/planted）加入会被拒绝，等下一回合。
- 离开队伍后保留背包和当前位置；比赛正常结束只把参赛者传回大厅，未参赛人员保持原地；终止比赛只让参赛者离队，不传送。
- 比赛结束后需要管理员重新开始。

### 4.1 用命令方块/按钮加入队伍

可以不放 `/trigger`，直接用命令方块执行加入函数。命令方块设为“需要红石”，旁边放按钮；按钮按下去时，命令方块以**最近玩家**（`@p`）的身份执行：

```mcfunction
# 加入 T
execute as @p run function cs2:join/t

# 加入 CT
execute as @p run function cs2:join/ct

# 加入解说
execute as @p run function cs2:join/cast

# 离开队伍
execute as @p run function cs2:join/leave
```

说明：

- 命令方块要放在按钮旁边，`@p` 才会选中按按钮的玩家；如果按钮和命令方块离玩家很远，可能选错人。
- 函数内部会处理准备阶段/回合中/比赛结束等状态，并自动 reset + enable 对应的 trigger，不需要额外写 `scoreboard players enable`。
- 服务器需要在 `server.properties` 里 `enable-command-block=true`；单人存档需要开启作弊。
- 如果想让按钮范围内所有玩家一起加入，把 `@p` 换成 `@a[distance=..5]`（注意会一次加入多人）。

### 4.2 实验性投掷物：烟雾弹 / 高爆手雷 / 闪光弹 / 燃烧弹

投掷物已加入商店的「投掷物」分类，也可用函数直接发给测试玩家：

```mcfunction
/function cs2:util/give_all
/function cs2:util/give_smoke
/function cs2:util/give_he
/function cs2:util/give_flash
/function cs2:util/give_fire
```

商店价格：烟雾弹 $300、高爆手雷 $300、闪光弹 $200、燃烧弹 T $400 / CT $500。

每种投掷物**每回合最多购买 1 个**；如果上局存活并带着同类投掷物，也视为已有，不能重复购买。

物品用法：

- 投掷物都是自定义雪球物品，**右键投掷**。
- 物理（速度/重力/反弹）、引信、爆炸/燃烧/烟雾/闪光效果，全部由 `cs2grenade` 模组处理；数据包只负责商店物品、发放函数和购买上限。
- 当前参数：投掷物初速 2.0、重力 0.04；烟雾云半径 6、持续 20 秒、烟卡 137 张；燃烧弹每 0.5 秒一跳，总伤 40，火区半径 4→9 格；高爆穿透 6、破片 2、破片范围 10。
- 闪光弹引信 1.5 秒，最大闪黑 5 秒；视线检测/黑屏由模组 `flash.json` 负责，不再依赖数据包射线函数。
- 需要资源包才有自定义物品模型。
- 服务端与客户端需安装 `cs2grenade`；使用闪光功能时按整合包说明安装幻幕展映。

当前限制：

- 高爆/燃烧的击杀归属由 `cs2grenade` 模组按投掷者写入伤害来源，正常会显示击杀者；只有 C4/环境伤害才显示“阵亡”。
- 烟雾/闪光为近似效果，不能完全等同于 CS2。
- 具体参数以 `data/cs2/grenade/*.json` 和 `cs2grenade` 的 `GrenadeManager` 为准。


管理员开始比赛：

```mcfunction
/function cs2:match/start
```

要求：五个配置点齐全，至少一名玩家加入 T 或 CT。**只有一方有玩家时也可以开始**，方便单人测试地图/炸弹流程；此时自动进入“测试模式”：

- 空的一方不会在回合开始时立刻被判负，也不会因为“存活人数为 0”而直接结束回合。
- 有人的一方全灭、时间耗尽、炸弹爆炸/拆除仍按正常规则结算（例如仅 T 时 T 全灭会判 CT 胜）。
- 仅 T 时下包后可以完整测试 40 秒炸弹倒计时与爆炸；测试模式会在开局提示与 `match/status` 中提示。

比赛流程：

1. 10 秒热身（warmup）
2. 每回合 25 秒准备（freeze，传送到出生点、禁止移动；准备阶段开始时清理地面掉落物；阵亡/新加入者清包并发默认手枪 + 零时大剑 + 弹药盒，存活者保留上局装备；发放商店钻石）
3. 准备阶段购买武器/护甲/拆弹器；超时未购买则用默认手枪 + 零时大剑开局
4. 115 秒 live；T 随机一人拿到 C4；CT 不再随机发拆弹器，需要自己购买
5. 下包后进入 planted，炸弹 40 秒倒计时
6. 回合结束 5 秒后自动下一回合，并按经济规则结算
7. 第 12 回合结束换边：比分随队伍交换；经济重置到手枪局（每人 $800、连败奖励回到 $1900、装备清空），第 13 回合重新从默认手枪开始

### 经济与商店

每名玩家的资金独立，初始 **$800**。准备阶段会自动收到默认手枪（T Glock-17 / CT M9A4）、零时大剑和创造模式全种类弹药盒；其余武器、护甲、拆弹器都要用商店钻石购买。

> 默认装备有兜底检查：如果玩家在 freeze、回合结束等阶段死亡，下一回合会按“阵亡/新加入”处理，清包并补发默认手枪、零时大剑和弹药盒；如果发现默认手枪或弹药盒缺失，也会自动补发。

准备阶段会发给每名参赛者一颗名为“商店”的钻石。手持钻石右键，打开箱子式商店界面；商品图标自带名称和 lore 提示。钻石没有放置/交互行为，不会像木棍那样被部分模组赋予额外用途。

商店只在准备阶段（state=2）可用；购买后立即扣款、发物品。商店界面由商店模组读取 `data/cs2/shop/*.json` 渲染，不再使用聊天栏按钮。

**购买规则：**

- 主武器可以重复购买并替换：会清除上一把主武器（不返还旧枪费用），再发新枪；副武器/默认手枪不会被误清。
- 副武器只能买一把；购买新手枪会清掉默认手枪。
- 默认手枪可以被新购手枪替换；没有购买则一直保留。
- 护甲分三档：无甲（$0）、半甲（头盔 + 胸甲，$650）、全甲（全套下界合金，$1000）。
- 拆弹器 $400，仅 CT 可买；购买后本回合持有 5 秒拆弹权限。
- 每回合开始，**上局存活者保留武器、护甲、拆弹器和购买状态**；只有阵亡/新加入者会清包并重新发默认手枪。C4 不跨回合保留，每局由随机 T 重新领取。
- **对局内为死亡不掉落**：`keepInventory` 始终为 `true`，死亡后武器/护甲/弹药保留；比赛结束、终止、回合结束都不清包。
- C4 携带者阵亡时从背包回收 C4，5 秒后补给仍存活的 T；拆弹器阵亡同样回收，炸弹已安放时补给存活 CT。两者都不会作为地面掉落物留下。
- 主武器拾取保持“一主武器”：已经持有一把时，新捡到的那把会以 5 秒拾取延迟弹出，旧枪保留；商店购买时不会误清新枪。
- 准备阶段开始时清理地面掉落物（`round/freeze` 开头 `kill @e[type=item]`）。

**武器价格（全部为 TACZ 物品）：**

| 类别 | 武器 | 价格 | 限制 |
|---|---|---:|---|
| 手枪 | 沙漠之鹰 | $700 | 通用 |
| 手枪 | CZ75 | $300 | 通用 |
| 手枪 | M1911 | $500 | 通用 |
| 手枪 | 手枪 | $100 | 通用 |
| 手枪 | 3 号左轮手枪 | $600 | 通用 |
| 手枪 | 截断莫辛纳甘 | $800 | 通用 |
| 冲锋枪 | MP5A5 | $1150 | CT |
| 冲锋枪 | UMP45 | $1050 | T |
| 冲锋枪 | 维拉佩罗萨 | $2000 | 通用 |
| 冲锋枪 | P90 | $2900 | 通用 |
| 冲锋枪 | P90（空尖弹） | $1500 | 通用 |
| 步枪 | 满改 AKM / AK-47 | $2700 / $2500 | T |
| 步枪 | 满改 SCAR-L / SCAR-L | $3200 / $2800 | T |
| 步枪 | 满改 M16A4 / M16A4 | $3200 / $2800 | CT |
| 步枪 | 满改 M4A1 / M4A1 | $3400 / $3000 | CT |
| 步枪 | 191 式 | $3100 | 通用 |
| 步枪 | AUG | $2400 | CT |
| 步枪 | G36K | $2200 | T |
| 步枪 | FN FAL | $3900 | 通用 |
| 霰弹枪 | M870 / AA12 / DB-4 | $1700 / $2250 / $1400 | 通用 |
| 霰弹枪 | M1014 / 满改 M1014 | $2950 / $3750 | 通用 |
| 霰弹枪 | M2火焰喷射器 | $4800 | T |
| 射手步枪 | SKS / SPR-15 HB | $4800 / $4800 | T / CT |
| 机枪 | M249 / 刘易斯 | $5000 / $4500 | CT / T |
| 机枪 | 95式班用机枪 | $4800 | 通用 |
| 狙击枪 | M1895 / 春田 1873 / AWM | $2550 / $2050 / $4750 | 通用 |
| 投掷物 | 烟雾弹 / 高爆手雷 | $300 / $300 | 通用 |
| 投掷物 | 闪光弹 | $200 | 通用 |
| 投掷物 | 燃烧弹 | T $400 / CT $500 | 通用 |

> 除手枪外，所有枪械价格已统一下调 $250；步枪在全局下调后再降 $50。本轮另对 M4A1/M16A4 降 $400、191 +$100、AUG -$100、部分霰弹/DMR/机枪再调价；以表为准。投掷物按 CS2 价格加入商店。
>
> 练习模式（只有一方有玩家）每回合把参赛者资金重置为 **$10000**，方便测试经济与购买。

**连败奖励：**

- 初始 $1900；每次连续失败 +$500，最高 $3400；每次获胜 -$1000，最低 $1400。
- T 因时间耗尽失败时，不增加连败数（仍发当前连败奖励）。
- 连败奖励跟随队伍，换边时交换。

**回合结算：**

| 条件 | 奖励 |
|---|---:|
| 回合全歼敌人 | $3250 |
| T 引爆炸弹获胜 | $3500 |
| CT 拆弹获胜 | $3500 |
| 安放炸弹者 | +$300 |
| T 安包后输掉，每名 T | +$600 |
| CT 每击杀一名 T，全队每人 | +$50 |

**击杀奖励：**

| 击杀武器 | 奖励 |
|---|---:|
| 霰弹枪 | $600 |
| 冲锋枪 | $400 |
| 机枪 / 手枪 / 步枪 / 射手步枪 | $200 |
| AWM | $100 |
| M1895 / 春田 1873 | $200 |
| 零时大剑 | $1500 |

击杀奖励通过 `player_killed_entity` + TACZ `GunId` 判定；只奖励合法击杀，友军击杀不奖励。

常用命令：

```mcfunction
/function cs2:match/status     # 查看比赛状态
/function cs2:match/stop       # 停止比赛、清理炸弹、仅参赛者离队（不清包/不传送）
```

### 战术菜单、暂停与投票

准备阶段和 live 阶段会给参赛者发一条 `[战术菜单]` 聊天按钮，也可以主动输入：

```mcfunction
/trigger cs2_panel set 1
```

- **战术暂停**：`/trigger cs2_pause`。只有**准备阶段**、参赛者、当前未暂停时可用；T/CT 各自最多 3 次，每次 30 秒。暂停时回合计时、炸弹倒计时、胜负检查都会停住，友伤暂时关闭；恢复后继续准备。
- **比赛暂停（独立于战术暂停）**：`/trigger cs2_match_pause`。只在**准备阶段**、双方都有玩家时可用；需要 **T/CT 各至少一人同意**才生效。生效后没有时间限制，任意参赛者可用 `/trigger cs2_match_resume` 或战术菜单里的“解除比赛暂停”随时解除。暂停期间同样停住计时/炸弹/胜负检查，并暂时关闭友伤。
- **投票重开本小局**：`/trigger cs2_vote_restart`。需要**所有 `cs2_inmatch=1` 的参赛者**同意；练习模式或暂停中不可用。
- **投票结束战局**：`/trigger cs2_vote_end`。准备阶段和 live 阶段都可以发起；需要**所有 `cs2_inmatch=1` 的参赛者**同意；练习模式或暂停中不可用。
- **练习模式退出**：准备阶段和 live 阶段的战术菜单里，`#solo=1/2` 时会显示 `[退出训练]`，等价于 `/trigger cs2_practice_leave`；只影响当前练习战局。

### HUD 显示开关

侧边栏已停用，回合、比分、存活、剩余时间/炸弹倒计时全部合并到**动作栏**。每名玩家可以独立开关自己的动作栏战报和回合/比赛标题：

```mcfunction
/function cs2:hud/toggle
/function cs2:hud/hide
/function cs2:hud/show
```

普通玩家（没有权限执行 `/function`）可以用触发器：

```mcfunction
/trigger cs2_hud_toggle
```

关闭后不会再收到动作栏和胜负标题，队友不受影响；BossBar（回合/炸弹计时）仍会显示。

> 首次进入服务器时，数据包会自动启用 `cs2_join_t`、`cs2_join_ct`、`cs2_join_cast`、`cs2_leave`、`cs2_hud_toggle`、`cs2_stats`，以及战术/练习触发器 `cs2_panel`、`cs2_pause`、`cs2_vote_restart`、`cs2_vote_end`、`cs2_practice_leave`；每次使用后也会自动重新启用。
>
> 如果仍然提示“无法触发该积分项”，说明当前存档里的触发器没有启用。管理员执行一次 `/reload`，或对单个玩家执行：
>
> ```mcfunction
> /scoreboard players enable @s cs2_join_t
> /scoreboard players enable @s cs2_join_ct
> /scoreboard players enable @s cs2_join_cast
> /scoreboard players enable @s cs2_leave
> /scoreboard players enable @s cs2_hud_toggle
> /scoreboard players enable @s cs2_stats
> /scoreboard players enable @s cs2_panel
> /scoreboard players enable @s cs2_pause
> /scoreboard players enable @s cs2_vote_restart
> /scoreboard players enable @s cs2_vote_end
> /scoreboard players enable @s cs2_practice_leave
> ```
>
> 注意：旧版本数据包升级到本版本后，必须 `/reload` 一次，才能补建金钱/购买计分项、`cs2_stats`、战术/练习 trigger，并重新启用它们。

### K/D 与击杀播报

每场比赛开始时（`match/start`）会把参赛者的击杀、死亡数清零；每小局准备阶段会给每名参赛者单独发一条本局 K/D 提示：

```text
第 3 回合  |  本局 K/D  2 / 1
```

击杀播报由服务端模组 `cs2grenade` 监听 `LivingDeathEvent`，直接取 `DamageSource` 的击杀者，因此同 tick 连杀、同归于尽、手雷/燃烧击杀都能正确归属。只发给 `cs2_inmatch=1` 的参赛者与 `cs2_cast` 解说。播报会带上击杀武器：

```text
[T] Alice [AK-47] 击杀了 [CT] Bob
[友伤] [T] Carol [M4A1] 击杀了 [T] Dave
[T] Alice [高爆手雷] 击杀了 [CT] Bob
[T] Alice [燃烧弹] 击杀了 [CT] Bob
Bob 阵亡
```

玩家可以随时查询自己的本局 K/D：

```mcfunction
/trigger cs2_stats
```

比赛结束时，所有参赛者还会再收到一次自己的本场 K/D，并列出本场击杀最多的玩家；并列第一时用同一个击杀王标签一起列出，不再按玩家逐个解析名字。

### 赛后音乐盒接口

比赛正常结束后，`match/end` 会调用 `cs2:music/play`。预设编号存放在 `cs2_cfg` 的 `#music_preset`：

- `0`：关闭，默认值；
- `1..`：留给自定义曲目。

当前没有配置任何曲目，函数只保留接口。添加曲目时在 `data/cs2/function/music/play.mcfunction` 里按编号加入 `playsound`，例如：

```mcfunction
execute if score #music_preset cs2_cfg matches 1 run playsound cs2:music.victory master @a
```

如果要只让参赛者听到，把 `@a` 改成 `@a[scores={cs2_inmatch=1}]`。

- 死亡数使用原版 `deathCount`，所以坠落、爆炸、友伤等所有死亡都会计入。
- 击杀者由 `cs2grenade` 的 `LivingDeathEvent` 读取 `DamageSource` 得到；没有玩家击杀者时显示原版死亡信息。不再使用 `player_killed_entity` 或 `cs2_recent_killer` 标签配对。
- 友伤击杀会显示 `[友伤]`，并计入 K/D；击杀奖金仍由数据包 `econ/kill/on_*` 按阵营结算，同队击杀不发奖金。
- 击杀播报要求死亡者是本场参赛者；未参赛玩家之间的大厅 PvP 不会触发播报。解说队伍只收播报，不计 K/D、不发奖金。

---

## 5. 规则细节

### PvP 规则

- **比赛进行中友伤开启**：`cs2_t`、`cs2_ct` 两个队伍的 `friendlyFire` 都为 `true`；同队也可以误伤队友。
- **回合结束展示阶段关闭 PvP**：`round/end` 会把三队 `friendlyFire` 设为 `false`，下一回合 `round/live` 再恢复 `true`。战术暂停期间也会暂时关闭，恢复时重新打开。
- 非比赛/大厅阶段仍由 `pvp/on` 维持 `friendlyFire=true`；全局 PvP 由 `server.properties` 的 `pvp=true` 控制。Java 版没有 `/gamerule pvp`，数据包不会改这个文件。若服务器把 `pvp` 设为 `false`，请改回 `true` 并重启。

### 速度与夜视 buff

- 比赛开始（热身阶段）起，所有 `cs2_inmatch=1` 的参赛者获得无限时长的 **速度 II** 和 **夜视**。
- 准备、live、回合结束、半场/加时休息期间都保留；每回合 `round/freeze` 会重新补一次，防止被其他插件清掉。
- 重连、准备阶段加入、比赛中加入时都会补发；阵亡进入旁观后夜视仍保留，`join/to_lobby` 离场时才会移除。
- 两个效果都不显示粒子；如需关闭夜视，删除 `data/cs2/function/util/speed.mcfunction` 里的 `function cs2:util/night_vision` 行并重新 `/reload`。若要连速度一起关闭，再删除 `effect give ... speed` 行。

### 准备阶段锁定跳跃

- 准备阶段会把参赛者的 `minecraft:generic.jump_strength` 基础值设为 `0`，防止在出生点跳跃/卡位。
- 进入 live 时恢复为默认 `0.42`；重连到准备阶段会重新锁定，离场/比赛结束会恢复。
- 相关函数：`data/cs2/function/util/jump_lock.mcfunction`、`util/jump_unlock.mcfunction`。
- 退出训练、投票结束战局、比赛结束、重连到已结束比赛时，统一调用 `util/clear_match_state`：清除准备阶段的 `slowness` / `resistance` 并恢复跳跃，避免减速和锁跳残留。
- 如果你给玩家改过自定义跳跃属性，请把 `jump_unlock.mcfunction` 里的 `0.42` 改成你的默认值。

### 回合胜负

| 情况 | 胜者 |
|---|---|
| T 全部阵亡（未下包） | CT |
| CT 全部阵亡（未下包） | T |
| 下包后 CT 全部阵亡 | T |
| 炸弹爆炸 | T |
| 炸弹被拆除 | CT |
| 时间耗尽且未下包 | CT |

下包后 T 全灭**不会**结束回合，CT 必须拆包或等炸弹爆炸。

### 阵亡观战

- 阵亡后自动切换到旁观模式，并锁定本队仍存活的随机一名队友（`/spectate`）。
- 本队已经没有存活队友时，视角回到大厅观战点，避免卡在空镜头。
- 阵亡者保留 `cs2_team`，下一回合开始会自动回到本方出生点。
- 比赛正常结束只把 `cs2_inmatch=1` 的参赛者传回大厅，未参赛人员保持原地，也不清空背包；终止比赛只让参赛者离队，不传送、不清包。
- 退出训练/结束战局/比赛结束会清除 `slowness`、`resistance` 并恢复跳跃；准备阶段掉线、比赛结束后重连也会在 `player/make_spec` 中补做同一套清理。

### 炸弹

- 物品：海螺壳（`minecraft:nautilus_shell`），带 `cs2_bomb` 自定义数据与食物组件；不会放置、无方块交互，也不是书。
- 长按右键 3.2 秒；在 A/B 包点 10 格内才会成功，否则 C4 退回背包。
- 安放后地面出现 C4 模型与粒子/音效，BossBar 显示 40 秒倒计时。
- 最后 10 秒 C4 模型会闪烁。
- 爆炸：20 格内致命，并清除 20 格内的地面掉落物；不破坏方块。

### 拆包

- CT 在炸弹 3 格内、保持潜行，累计 10 秒。
- 持有拆弹器的 CT 只需 5 秒。
- 松开潜行或离开 3 格范围，进度清零。
- 拆包成功立即 CT 获胜。
- 进入 3 格范围后，动作栏会提示“按住 Shift 拆弹”；开始潜行后显示“拆弹中...”，方便确认是否生效。
- 拆弹进度会持续累计；松开潜行、离开 3 格、阵亡或炸弹被安放时清零。此前“只显示拆弹中但拆不掉”的原因是进度每 tick 被清零，已修复。
- 1.21.1 的玩家 NBT 不一定包含 `Sneaking` 字段，因此拆弹检测使用原版 `minecraft.custom:minecraft.sneak_time` 统计的每 tick 增量，而不是 `nbt={Sneaking:1b}`。

### 死亡掉落与 keepInventory

- **死亡不掉落模式（已回滚）**：`load`、`round/freeze`、`round/live`、`round/end`、`match/start`、`match/end`、`match/stop` 都保持 `gamerule keepInventory true`；对局内不再强制改成 false。
- **T 携带 C4 阵亡**：C4 从尸体背包回收，5 秒后补给仍存活的 T；不会掉在地上。
- **CT 携带拆弹器阵亡**：拆弹器回收；若炸弹已安放且地面没有拆弹器，会补发给存活 CT。
- **护甲/副武器/弹药盒/零时大剑/主武器**：正常死亡不会掉落；`util/item_tick` 与主武器拾取限制保留，用于处理手动丢弃/特殊情况，不会影响 normal death。
- **准备阶段开始清地面物品**：`round/freeze` 开头 `kill @e[type=item]`，在任何人购买前清掉上一局残留。

### 商店与配装

- 默认武器：T `tacz:glock_17`、CT `tacz:m9a4`，另有零时大剑 `claymore:zerotime`；每回合 freeze 自动发放，不能卖。
- 其他武器全部在 `shop/items/*.mcfunction` 中，购买时扣款并给对应 TACZ 枪。
- 护甲：`shop/items/armor_half.mcfunction`（半甲）、`armor_full.mcfunction`（全甲）。
- 拆弹器：`shop/items/kit.mcfunction`（$400，CT 专属）。
- 商店钻石与商店模组 JSON：`util/give_shop_item.mcfunction`、`data/cs2/shop/*.json`；旧 `shop/menu`、`shop/category/*`、`shop/trigger` 文件仅作历史保留，不再自动调用。
- 默认装备：`shop/default_t.mcfunction`、`shop/default_ct.mcfunction`。
- 经济结算：`econ/**.mcfunction`（初始 800、连败奖励、回合/击杀奖金）。

要换枪或改价格，直接改 `F:\DSH-Workspace\gen_shop.py` 里的 `ITEMS` 列表，然后运行：

```powershell
python F:\DSH-Workspace\gen_shop.py
```

它会重新生成整个 `shop/` 目录，避免手改几十个文件时漏掉分类/触发编号。

---

## 6. 配置

配置存放在计分板 `cs2_cfg` 的假玩家分数中，单位为**秒**（`kit` 为 0/1 开关）。默认值：

| 配置项 | 默认 | 含义 |
|---|---:|---|
| `warmup` | 10 | 赛前热身 |
| `freeze` | 25 | 每回合准备 |
| `round` | 115 | 回合时间 |
| `round_end` | 5 | 回合结束展示 |
| `bomb` | 40 | 炸弹倒计时 |
| `defuse` | 10 | 无拆弹器拆包时间 |
| `kit_time` | 5 | 有拆弹器拆包时间 |
| `overtime` | 1 | 12:12 是否进入加时（1=是，0=平局结束） |

修改示例：

```mcfunction
/scoreboard players set round cs2_cfg 90
/scoreboard players set bomb cs2_cfg 35
```

修改会在下一回合/下一次下包时生效。恢复默认：

```mcfunction
/function cs2:config/defaults
```

> 注意：`config/defaults` 会同时重置比赛内部状态（比分、回合、炸弹），建议在 idle 状态执行。

以下两项**当前固定，不能通过计分板修改**：

- 包点半径：10 格
- 爆炸致命半径：20 格；20 格内地面掉落物一并清除

这是 1.21.1 命令选择器不能直接读取计分板数值导致的限制。若要改，需要改函数里的 `distance=..10` / `distance=..20` 等数字。

---

## 7. TACZ 配装

所有购买武器和默认手枪都使用 TACZ 1.21.1 数据组件格式，不再使用原版武器占位。

### 生成文件

| 文件 | 内容 |
|---|---|
| `shop/default_t.mcfunction` | T 默认 Glock-17 + 创造弹药盒 |
| `shop/default_ct.mcfunction` | CT 默认 M9A4 + 创造弹药盒 |
| `shop/items/*.mcfunction` | 全部购买条目（手枪/冲锋枪/步枪/霰弹/射手步枪/机枪/狙击/投掷物） |
| `shop/items/armor_half.mcfunction` | 半甲 $650（下界合金头盔 + 胸甲） |
| `shop/items/armor_full.mcfunction` | 全甲 $1000（全套下界合金） |
| `shop/items/kit.mcfunction` | 拆弹器 $400（CT） |

价格、阵营限制、TACZ SNBT 全部在 `gen_shop.py` 的 `ITEMS` 列表里；改完运行 `python gen_shop.py` 重新生成。

### 1.21.1 数据组件格式

以 Glock-17 为例，正确写法：

```mcfunction
/give @s tacz:modern_kinetic_gun[minecraft:custom_data={GunCurrentAmmoCount:17,GunFireMode:"SEMI",GunId:"tacz:glock_17",HasBulletInBarrel:1b}]
```

弹药盒（默认发放到副手，避免占用背包格）：

```mcfunction
/item replace entity @s weapon.offhand with tacz:ammo_box[minecraft:custom_data={AllTypeCreative:1b}]
```

1.20.1 的旧 NBT 写法：

```mcfunction
/give @p tacz:modern_kinetic_gun{GunId:"tacz:m4a1",GunFireMode:"AUTO"}
```

**1.21.1 不能直接用**，必须放进 `minecraft:custom_data` 组件。

### 如果移植版结构不同

不同 TaCZ 1.21.1 移植版的组件名可能略有差异。若枪能取出但不能射击，先在游戏内用 `/data get entity @s SelectedItem` 检查数据是否落在 `minecraft:custom_data` 下；如果结构不同，改 `gen_shop.py` 后重新生成 `shop/`，不要手改 32 个生成文件。


---

## 8. 文件结构

```
cs2_bomb_datapack/
├─ pack.mcmeta
├─ README.md
└─ data/
   ├─ minecraft/tags/function/
   │  ├─ load.json
   │  └─ tick.json
   └─ cs2/
      ├─ advancement/bomb/
      │  ├─ plant.json
      │  └─ pickup.json
      ├─ advancement/econ/
      │  ├─ kill_t.json
      │  └─ kill_ct.json
      └─ function/
         ├─ load.mcfunction
         ├─ tick.mcfunction
         ├─ config/defaults.mcfunction
         ├─ setup/...
         ├─ match/...
         ├─ round/...
         ├─ player/...
         ├─ join/...
         ├─ shop/...
         ├─ econ/...
         ├─ kit/...
         ├─ loadout/...
         ├─ bomb/...
         └─ hud/...
```

主要入口：

- `cs2:load`：初始化
- `cs2:tick`：主循环
- `cs2:match/start`：开始比赛
- `cs2:setup/*`：地图配置
- 商店钻石：准备阶段自动发放，手持右键打开商店模组界面
- `cs2:shop/trigger`：商店触发器分派
- `cs2:econ/start`：初始化经济

---

## 9. 已知限制

- 经济已改为逐玩家金钱/商店；不再有 ECO/强起/长枪/狙击四套等级选择。
- 商店购买依赖 TACZ 1.21.1 移植版；测试服无 TACZ 时 `shop/items/*` 会报 `Unknown item`，这是预期，不是数据包损坏。
- 不自动平衡人数；需要玩家自己 `/trigger` 选边。
- 没有客户端模组按键；下包用“长按右键吃 C4”实现，拆包用“潜行”实现。
- 拆包检测基于 `sneak_time` 统计增量；若动作栏没有出现“按住 Shift 拆弹”，通常是距离超过 3 格，或玩家不在 CT 队伍/已阵亡。
- 重连不再清空 buff 或强制切成生存模式：存活参赛者保留背包/武器/护甲/效果，并按当前阶段回到本方出生点；未参赛者不会被自动传送或清包，阵亡/新加入者才走观战/清包路径。`check_seen` 现在登录后 2 tick 内处理，避免登录后短暂保持服务器存档的生存模式。
- 包点半径 10、爆炸半径 20 固定。
- 资源包改 C4/拆弹器与烟/雷/闪/火投掷物的模型和贴图，不提供音效/动画；资源包未更新时投掷物会退回雪球外观。
- 配装依赖 TACZ 1.21.1 移植版；本机测试服未安装该模组，因此 TACZ 枪械的发放/射击未在本机验证。若移植版物品 ID/组件不同，改 `gen_shop.py` 后重新生成 `shop/`。
- **侧边栏已停用**：所有信息整合到动作栏；`cs2_hud_toggle` 对每名玩家独立生效。旧存档的侧边栏会在每次 `/reload` 时自动清理。

## 10. 测试状态

已在真实 Minecraft 1.21.1 专用服务端完成：

- 数据包 `/reload` 成功，advancement JSON 解析通过；无 TACZ 测试服上仅 `shop/items/*`、`shop/default_*`、`econ/kill/reward` 与旧 `loadout/*` 因 `Unknown item` 报错，属预期
- 已将 TACZ 物品临时替换为 `minecraft:paper` 后 `/reload`，所有函数与 advancement 均成功加载，确认上述报错仅来自测试服缺少 TACZ，而非命令/SNBT 语法
- 已用 `minecraft:paper` 验证 `custom_data~{GunId:...}` 的部分匹配写法可被 1.21.1 解析
- 核心函数在无玩家环境下执行，未出现运行时错误
- 已验证：食物组件、自定义数据、C4 物品展示实体、文字展示实体、BossBar、队伍、计分板、`tp` 到 marker 复制朝向
- 已验证 `cs2_join_t` / `cs2_join_ct` / `cs2_leave` / `cs2_hud_toggle` 触发器的创建、旧存档升级补建
- 已验证无玩家时 `match/start` 会拒绝开始；单方测试模式的 `#solo` 分支已做代码审查，但未接入真实客户端跑完整单方回合
- 已验证动作栏 8 个状态的 JSON 解析、`match/end` 只把参赛者传回大厅、侧边栏清理命令
- 已验证战术暂停只在准备阶段可用：`pause_request` / `pause_start` / `pause_tick` / `resume` 可在无玩家环境执行，`#paused` 正常置 1/清 0；真实玩家点击、投票计数和练习退出仍需实机验证
- 商店购买、每玩家资金/连败/击杀奖金**尚未在真实玩家上跑完整回合验证**；只能确认函数/菜单 JSON 已加载（除 TACZ 物品本身）

由于测试服没有接入客户端玩家，以下交互仍需实机验证：

- 玩家长按右键吃 C4 / 下包
- 潜行拆包与进度重置（已改为 `sneak_time` 统计检测；无真实玩家，仍需实机确认）
- 拾取/丢弃 C4 的 advancement 触发
- 死亡→观战→下一回合回到出生点
- 重连到大厅/准备/live 存活/live 阵亡/回合结束时，buff、背包、游戏模式与商店钻石是否符合预期
- 每名玩家独立打开商店、购买扣款、余额显示、私密购买（队友/敌人不可见）
- 实际执行 `/trigger cs2_join_t` / `/trigger cs2_join_ct` / `/trigger cs2_hud_toggle`
- TACZ 枪械实际发放、开火、换弹与击杀奖励（需装有 TACZ 的客户端验证）

建议先开创造模式，用 1–2 名玩家跑一遍完整回合，再上正式服。
