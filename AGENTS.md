# AGENTS.md — 协作流程（人类与 AI 代理都按此执行）

本仓库用于同步 CS2 数据包、服务端模组与客户端资源包。任何改动都必须先有 Issue，再落实到提交。

## 0. 角色

- 维护者：`sswer154`（决定谁做、合并与最终数值）
- 协作者：`NanamiManatsu`（数据包 / 模组 / 资源包实现）
- 如果后续加入其他人或代理，同样遵循下面的认领规则。

## 1. 发现问题：先建 Issue

- 发现 bug、需求或数值调整，第一步是在本仓库建 Issue，不要在聊天里口头约定后就改代码。
- 标题格式：`<类型>: <一句话描述>`，类型用 `bug` / `feat` / `tune` / `docs`
- 正文必须包含：现象或目标、影响的文件或系统（数据包 / 模组 / 资源包）、复现步骤（bug）、期望结果、已知的数值参数（例如引信 tick、初速、伤害）
- 建好后用 `gh issue list` 确认编号，后续所有提交都要引用它。

```bash
gh issue create --title "bug: 烟雾弹落地后不放烟" --body-file issue.md
```

## 2. 认领：明确谁做

- 谁决定做，就在 Issue 里 `@` 自己并留下认领评论，然后把自己设为 assignee。
- 命令：

```bash
gh issue comment <编号> --body "认领，我来做。预计改动：数据包 smoke.json / GrenadeManager"
gh issue edit <编号> --add-assignee <github账号>
```

- 一个 Issue 同时只由一个人做。需要别人接手时，在 Issue 里说明并改 assignee，不要两个人同时改同一个文件。
- 维护者可以指定负责人；被指定的人在 Issue 里回复确认后再动手。

## 3. 开发与本地验证

- 从 `master` 拉分支：`git switch -c fix/<issue编号>-<短描述>`
- 数据包改完要在本地服务器 `/reload` 验证；模组要 `gradle build` 并部署；资源包要客户端 F3+T 后确认。
- 验证结果（通过 / 失败原因）写回 Issue，不要只在本地口头确认。

## 4. 推送前：先检查仓库是否已同步

推送前必须执行，顺序不能跳：

```bash
git fetch origin
git log --oneline HEAD..origin/master     # 远端有没有新提交
git rebase origin/master                  # 有就先变基
```

- 如果出现冲突，停下来在 Issue 里说明冲突文件，不要强推。
- 变基后再跑一次 `git diff --stat origin/master` 确认只包含本次 Issue 相关的文件。
- 禁止 `git push --force` 到 `master`。

## 5. 推送：提交信息引用 Issue

- 提交标题：`<类型>: <描述> (refs #<编号>)`
- 正文写清：改了哪些文件、关键数值、验证方式
- 推送后在本 Issue 里回一条变更说明，格式见下节。

## 6. 推送后：发布本次修改内容

在对应 Issue 下回复，至少包含：

1. 提交哈希与分支
2. 变更文件清单（分组：数据包 / 模组 / 资源包）
3. 关键参数变化（如引信 50→30 tick、初速 1.85→2.0）
4. 验证方式与结果
5. 需要对方做的事（重载、替换 jar、客户端重载资源包）

模板：

```markdown
已推送 `<hash>`。
- 数据包：`grenade/flash.json`（fuse 50→30）
- 模组：无
- 资源包：无
验证：本地服务器 /reload 后扔闪光，1.5 秒爆炸。
需要你做：服务器拉取数据包并 /reload。
```

## 7. 文件归属，避免冲突

- 数据包参数（`data/cs2/grenade/*.json`、`shop/*.json`）与模组代码（`cs2grenade`、`cs2shop` 的 java 源码）尽量由不同人负责同一时间只改一处。
- 同一文件中不要两人并行修改；需要并行时先拆 Issue。
- 第三方模组 jar 不纳入仓库，只保留 `cs2grenade-*.jar` 与 `cs2shop-*.jar`。

## 8. 同步方向

- 以本仓库 `master` 为准。服务器上的运行版本应与 `master` 一致；不一致时先确认以哪边为准，再执行同步。
- 仓库自带 `sync.ps1`（Windows）用于把服务器上的数据包 / jar / 资源包复制进仓库并提交，默认不推送：

```powershell
.\sync.ps1 -Title "sync: ..." -Body "数据包：...；模组：..."        # 只提交
.\sync.ps1 -Title "sync: ..." -Push                              # 提交并推送
```

## 9. AI 代理必须遵守的硬性约束

- 没有 Issue 不开工；没有认领不改文件。
- 推送前必须执行第 4 节的 fetch / rebase 检查，并在回复中报告结果。
- 推送后必须按第 6 节发布变更内容。
- 不确定谁负责、或涉及覆盖他人改动时，停下来询问，不要自行决定。
