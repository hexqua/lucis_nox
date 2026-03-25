# Backport Checklist

## 標準手順

1. `main` の変更から、取り込み候補の非 `merge` コミットを列挙する。
2. 擬似スカッシュではないことを確認し、実際に取り込む SHA を確定する。
3. 1 機能を構成する連続コミット列だけを `git cherry-pick -x <sha1> [<sha2> ...]` で取り込む。
4. コンフリクト時は `1.20.1-main` 側で必要な Forge 実装を優先し、NeoForge 前提の接着コードを戻さない。
5. datagen や resource の削除・改名・移動を含む場合は、先に cleanup 範囲を決める。
6. 必要に応じて `./gradlew.bat runData` を実行し、生成物を再確認する。
7. `git diff --name-status` を確認し、不要差分、取り込み漏れ、古い生成物の残留がないことを確認する。
8. `./gradlew.bat build` を実行し、対象ブランチで検証を完了する。

## 実務ルール

- `merge` コミットは直接取り込まない。
- 擬似スカッシュコミットを backport 単位として扱わない。
- 無関係な整形、rename、大規模移動が混ざる変更は、そのまま移さず必要部分を切り出す。
- `1.20.1-main` 固有の暫定回避は、その枝で閉じる前提で扱う。
- remote への非 GET 操作は人間のみが行う。

## 読み分け

- generated や手置き JSON が絡む場合は `generated-cleanup.md` を読む。
- Forge / NeoForge の API や登録差分が絡む場合は `loader-diff-checks.md` を読む。
