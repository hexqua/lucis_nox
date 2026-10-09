---
name: backport-ready-development
description: "`main`（Minecraft 1.21.1 / NeoForge）の変更を、将来 `1.20.1-main`（Minecraft 1.20.1 / Forge）へ backport しやすい設計・差分・コミット列に保つ。共通機能や共通不具合修正、registry、datagen/resource、依存変更、refactorを計画・実装・レビューするときに使用する。実際の cherry-pick/backport、1.20.1固有修正、明確な1.21.1 / NeoForge専用変更には使用しない。"
---

# Backportを考慮したmain開発

`main`での実装品質を優先しながら、1.20.1へ運べる意図と差分境界を保つ。

通常開発では未コミットで人間へ引き渡す。コミット構成の検討はコミット作成の許可を意味せず、実際のbackport依頼だけに適用される例外を通常開発へ広げない。

## 手順

1. Backport候補かを判断する。
   - 共通機能と共通不具合修正を候補とし、1.20.1側での利用価値と移植コストから選択する。
   - Minecraft、NeoForge、外部MODの1.21.1固有仕様だけに依存する変更は対象外とし、理由を明示する。
   - 実際の取り込み依頼では、`1.20.1-main`から分岐した作業ブランチで、そのブランチの`AGENTS.md`と`.codex/skills/backport-1-20-1-forge`を読む。実作業用スキルは1.20.1側で管理する。
2. 実装前に変更面を分類する。
   - 共通ロジック
   - 1.21.1 / NeoForge固有の接着コード
   - generated/resourceとdatagen
   - テストとドキュメント
   - backport対象外
3. プレイヤーから見た契約を固定する。
   - content ID、設定キー、保存データ、主要な動作を可能な範囲で維持する。
   - 1.20.1で同じ実装ができない場合は、同じ動作を再実装するのか、部分対応または対象外にするのかを先に記録する。
4. 差分とコミット境界を整える。
   - 共通ロジックとNeoForge固有接着コードを、ビルド可能性を壊さない範囲で分ける。
   - 無関係な整形、rename、広域整理を混ぜない。
   - generated/resourceの削除・改名・移設は、追加差分に埋もれない形で残す。
   - 分離するとコミットが成立しない場合は、無理に分けずtarget側で必要な補正を説明する。
5. 実装とレビューを行う。
   - 外部MOD仕様、workaround、server/client差、実行順依存には「なぜ必要か」を日本語コメントで残す。
   - backportのためだけにmainの自然なNeoForge設計を崩さない。target固有の補正はbackport時に行う。
   - `git diff --name-status`とコミット列から、移植対象、target側補正、対象外要素を説明できる状態にする。
6. main側で通常の検証を行う。
   - `AGENTS.md`に従い、影響範囲に応じたbuild、GameTest、optional MOD検証を実行する。
   - このSkillの使用だけを理由にJava 17検証や1.20.1側の作業を開始しない。

## 参照

- コミットの計画・分割・レビューでは[コミット構成](references/commit-shaping.md)を読む。
- registry、event、設定、client初期化、loader APIを触る場合は[Loader境界](references/loader-boundary.md)を読む。
- datagen、JSON、tag、削除・改名・配置変更を含む場合は[Resource差分](references/resource-diff-hygiene.md)を読む。
