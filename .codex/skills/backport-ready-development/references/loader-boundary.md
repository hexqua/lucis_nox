# Loader境界

## 分離対象

- registry登録
- event bus購読
- capability、attachment、data component
- 設定と依存定義
- datagenの入口とrun設定
- client初期化とclient専用参照
- NeoForgeまたはMinecraft 1.21.1固有API

## 判断基準

- プレイヤーから見た動作と、NeoForgeでその動作を接続する実装を分けて考える。
- 共通計算、状態判定、ID、テスト可能な契約はloader非依存に保てるか確認する。
- NeoForgeの自然なAPI利用を避けるためだけの抽象化は追加しない。
- 1.20.1で別実装が必要な場合は、target側で使うForge hookやoverrideの候補と、同一動作を確認する方法を記録する。

## コメント

- 外部MODやvanillaの仕様により接着コードが必要な理由を残す。
- 1.20.1へそのまま移せない場合は、構文差ではなく意味上の差を説明する。
- 単なるAPI名の違いは、将来の推測コメントとしてソースへ残さずレビュー結果で扱う。
