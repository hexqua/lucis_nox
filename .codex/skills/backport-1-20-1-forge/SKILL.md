---
name: backport-1-20-1-forge
description: 1.21.1 NeoForge の `main` から 1.20.1 Forge の `1.20.1-main` へ変更を backport するときの標準手順。`git cherry-pick -x`、個別 SHA の選定、NeoForge/Forge 差分の確認、generated cleanup、build 検証、擬似スカッシュや merge commit の回避が必要な作業で使う。
---

# Backport 1.20.1 Forge

## Overview

`main` から `1.20.1-main` へ変更を戻すときの専用手順を扱う。通常の実装タスクとして進めず、対象 SHA の確定、取り込み順、loader 差分確認、generated cleanup、build 検証を先に固定する。

## Quick Start

1. 取り込み対象の非 `merge` コミットを列挙し、個別 SHA を確定する。
2. `references/port-checklist.md` を読み、`cherry-pick`、cleanup、検証までの順序を固定する。
3. datagen や手置き JSON を触る変更なら `references/generated-cleanup.md` を読む。
4. API 差分、イベント登録、mods.toml 系、worldgen、tag/loot/recipe の差分があるなら `references/loader-diff-checks.md` を読む。

## Workflow

### 1. 取り込み対象を絞る

- `merge` コミットはそのまま取り込まない。
- 擬似スカッシュコミットも backport 対象にしない。
- 1 機能を構成する連続コミット列を維持し、`git cherry-pick -x <sha...>` で取り込む。
- 無関係な整形、rename、広域整理が混ざる場合は、先に対象コミットを分割するか、必要箇所だけを別コミットとして作り直す。

### 2. 先に止まる条件を確認する

- `1.20.1-main` がまだ存在しない場合は、branch の作成や remote への push を自分で進めず、人間に確認する。
- remote への非 GET 操作は行わない。`git push`、PR 作成、remote branch 更新は人間に委ねる。
- 1.20.1 Forge 側でしか成立しない暫定回避を `main` に逆流させない。

### 3. 差分を loader/version 観点で確認する

- NeoForge と Forge で event bus、登録、設定ファイル、依存定義、datagen 入口が一致すると決めつけない。
- 単純な `cherry-pick` で済まない場合は、共通ロジックだけを手で移し、接着コードは `1.20.1-main` 側で書き直す。
- 詳細な確認観点は `references/loader-diff-checks.md` を使う。

### 4. generated と手置き resource を検証する

- 削除、改名、出力先変更、旧書式からの移行がある場合は、`runData` や build の前に stale 出力の混入を疑う。
- `src/generated/resources` だけでなく、手置き JSON や build 出力側も確認する。
- 迷ったら `references/generated-cleanup.md` を読む。

### 5. 最後に検証する

- `git diff --name-status` で取り込み漏れや不要差分を確認する。
- `./gradlew.bat build` を対象ブランチで成功させる。
- 必要なら `runClient` を使うが、GUI 起動が不要な確認では build 成功を優先する。

## References

- 標準手順: `references/port-checklist.md`
- generated cleanup: `references/generated-cleanup.md`
- Forge / NeoForge 差分確認: `references/loader-diff-checks.md`
