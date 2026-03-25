# Forge / NeoForge Diff Checks

## 基本原則

- 1.21.1 NeoForge と 1.20.1 Forge は loader も Minecraft 本体も違うため、Java 側の差分が小さく見えても接着コードは別物として扱う。
- `cherry-pick` は共通ロジックを運ぶための手段であり、loader 固有実装までそのまま成立すると期待しない。
- 迷ったら「共通ロジックだけを運び、登録や起動周りは 1.20.1-main 側で書き直す」を優先する。

## 確認観点

- メイン mod クラスのアノテーション、コンストラクタ、event bus 受け取り方
- registry の登録 API とイベント購読方法
- `mods.toml` / `neoforge.mods.toml` など loader 定義ファイル
- `build.gradle` と `gradle.properties` の loader/version 依存
- datagen のエントリポイント、run 設定、generated 出力先
- worldgen、tag、loot、recipe など data/resource の書式差分
- クライアント専用参照が server で漏れないか

## 判断ルール

- 共通ロジックと loader 接着コードを同一コミットで無理に運ばない。
- NeoForge 固有 API をそのまま Forge 側へ寄せるのではなく、1.20.1 Forge 側の正しい実装へ置き換える。
- `main` 側で成立しているだけの補助コードを、1.20.1 側へ不要に持ち込まない。

## 最低限の検証

- `./gradlew.bat build` が通ること
- 登録漏れがないこと
- client 専用参照が server 前提コードへ混ざっていないこと
- data/resource の path や namespace に旧残骸がないこと
