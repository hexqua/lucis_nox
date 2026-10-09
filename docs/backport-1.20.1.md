# 1.20.1 / Forge ブランチの運用

`main` は Minecraft 1.21.1 / NeoForge / Java 21 の主系統、`1.20.1-main` は Minecraft 1.20.1 / Forge / Java 17 の移植先とする。共通機能・修正は原則として main で開発し、1.20.1 固有の補正は移植先で維持する。

## 初回移植

両ブランチの起点は `d738a7d7d1299ca6d25de0d1632ea03acf9e6994`。既存の履歴を継承して分岐し、`port/backport_initial` で Forge 対応の差分を作成する。この作業ブランチから `1.20.1-main` への PR を人間が作成・マージする。初回は過去の全コミットを再 cherry-pick しない。

開発基盤は Forge 47.4.10、ForgeGradle 6.0.54、Gradle 8.8、Java 17、Parchment 2023.09.03-1.20.1。依存 MOD の採用版は `gradle.properties` を正とし、Forge 向け GeckoLib・Patchouli・Curios を使用する。前例 apprentice_codex の Forge 設定を参考にし、固有の optional MOD や Mixin は持ち込まない。

瓶の Data Components を `BlockEntityTag` NBT へ移し、蓄積量の保存・回収・再設置を維持する。アイテム化時には能動生成状態を破棄する。コンテンツ ID と生成量・容量・投入条件は主系統と共通にする。1.21.1 のワールドを 1.20.1 へ直接ダウングレードする運用は含めない。

## 継続取り込み

1. 移植先の作業ブランチで実移植用 `.codex/skills/backport-1-20-1-forge` を読む。
2. main の非 merge コミットを機能・修正単位で選び、個別 SHA と順序を確定する。
3. `git cherry-pick -x <sha...>` を使う。競合は Forge と Java 17 の構成を維持して解消する。
4. generated/resource の変更を含む場合は旧出力を確認し、移植先で runData を実行する。
5. build、GameTest、IDE Inspection とローカルレビューを終え、人間が PR 経由で移植先へ取り込む。

両基準ブランチを直接 merge しない。merge コミットの cherry-pick や擬似スカッシュを移植単位にしない。Forge 固有の補正と main の運用専用変更は機械的に同期しない。

## generated と開発実行

1.20.1 は `recipes`、`loot_tables`、`advancements`、`tags/blocks`、`structures` のパスを使用する。Biome modifier は `forge/biome_modifier` と `forge:add_features`。旧 1.21.1 の単数パス・NeoForge 定義が source と jar に残らないことを確認する。手置き assets と generated を区別し、削除対象は絶対パスが作業リポジトリ内であることを確認する。

Java の切替は `.\scripts\use-java.ps1`（このブランチの既定は 17）。IDE の Project SDK / Gradle JVM も Java 17 とし、実行構成は `./gradlew.bat genIntellijRuns` で生成する。main との開発環境混在を避けるため、別 worktree・IDE ウィンドウを利用できる。

GameTest は `src/gameTest` の source set を専用実行だけに登録し、配布 jar に含めない。`run/gametest/world` はテスト前に削除し、通常の `run/world` は保持する。同じ実行ディレクトリの Gradle/server は直列に終了を追跡する。

## 検証と公開

runData の出力を確認し、Java 17 で `build` と `runGameTestServer` を実行する。配布 jar の再難読化、`META-INF/mods.toml` の展開・依存範囲、テスト非同梱、旧 resource の残留を確認する。GameTest の初回失敗は後続成功で省略しない。

client の表示・操作・同期は人間が確認する。初回は瓶の透明描画・能動生成・回収と再設置・tooltip/bar、鉱石の生成とドロップ、専用 server 接続を確認する。自動検証の成功だけで表示を確認済みにしない。

CI はこのブランチの Java 17 と main の Java 21 を分ける。GitHub の required check は実際の CI 成功を確認して人間が設定する。push、PR 作成、merge、remote 設定変更は人間が行う。通常修正は人間確認後の明示指示まで未コミットとし、明示 backport の対象側コミットは AGENTS の例外に従う。

調査・作業途中のログは `.tmp` に置き、本書には採用済みの運用だけを残す。
