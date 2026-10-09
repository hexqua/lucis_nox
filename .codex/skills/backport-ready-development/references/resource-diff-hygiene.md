# Resource差分

## 基本原則

- `main`では1.21.1の配置とschemaを正とし、1.20.1向け旧形式を混在させない。
- 削除・改名・移設は、追加差分に埋もれず`git diff --name-status`で追跡できる状態にする。
- `src/generated/resources/.cache`だけでは、branch切替やcherry-pickで入ったstale JSONが消えない前提で扱う。

## 実装時の確認

- datagen providerの変更とgenerated出力の対応を確認する。
- 手置きJSONはdatagen管理外として、旧配置の削除漏れを別途確認する。
- recipe、tag、loot、advancement、enchantmentなど、バージョン間で配置やschemaが変わる種類を明示する。
- `data/<namespace>/recipe`、NeoForge condition、data componentなど、1.20.1で補正が必要になる要素をレビュー結果へ残す。

## レビュー結果

- Backport対象となるresourceと、1.20.1側で再生成または再記述するresourceを分ける。
- 削除・改名された旧pathと、stale出力の確認範囲を示す。
- この準備作業では1.20.1 datagenやcleanupを開始せず、対象resource、旧path、stale出力の確認範囲を1.20.1側の作業へ引き継ぐ。実作業では`1.20.1-main`から分岐した作業ブランチの`.codex/skills/backport-1-20-1-forge`を使用する。
