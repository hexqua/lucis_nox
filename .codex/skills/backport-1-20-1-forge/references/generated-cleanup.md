# Generated Cleanup Guide

## 前提

- branch 切替、手動コピー、`cherry-pick` 後の build では、古い generated や build 出力がそのまま残ることがある。
- datagen を再実行すれば必ず整うと決めつけない。
- `src/generated/resources` と手置き resource は別物として確認する。

## 先に疑うケース

- JSON を削除した。
- 出力先ディレクトリを変えた。
- 命名規則や path を変えた。
- datagen 管理と手置き管理の境界を変えた。
- NeoForge 側の resource 配置を Forge 側で調整した。

## 確認手順

1. 影響ディレクトリを先に決める。
2. generated 由来か手置き resource 由来かを切り分ける。
3. 必要なら影響範囲の stale 出力を掃除してから再生成する。
4. `git diff --name-status` で、不要ファイルが削除差分として見えていることを確認する。
5. build 後に source 側と出力側の両方を確認し、旧 path の残留がないことを確認する。

## 注意点

- `src/generated/resources` にない古いファイルが build 出力側へ残ることがある。
- 手置き JSON は datagen では直らないため、移動や削除を別途確認する。
- 旧 ID や旧 path の残骸があると、1.20.1 側だけでロードされる事故が起こりやすい。
