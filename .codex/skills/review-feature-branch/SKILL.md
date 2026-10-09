---
name: review-feature-branch
description: "機能ブランチ全体をbase branchと比較し、PR作成前またはマージ前のreadinessを読み取り専用でレビューする。累積差分、コミット列、テスト・ドキュメント、GitHub Actions、Codex Cloud、人間レビュー、未解決指摘を確認するときに使用する。単一の未コミット実装レビューには使用せず、ファイル修正、PRコメント、commit、push、mergeは行わない。"
---

# 機能ブランチレビュー

単体コミットでは見えない累積差分と外部レビュー状態を確認し、PRまたはマージの可否を判定する。

## 手順

1. 比較範囲を固定する。
   - PRが存在する場合はPRのbase branchを使用する。
   - PRがなくユーザー指定もない場合は、`main`、`1.20.1-main`などの保守対象branchとの祖先関係から分岐元を特定する。
   - 複数候補が残る、または分岐元を確定できない場合は、比較を開始せずbase branchを確認する。
   - merge-base、`base..HEAD`の非mergeコミット列、累積diff、作業ツリー状態を確認する。
   - 未コミット変更がある場合はreadiness上の未確定要素として扱い、必要なら`review-local-change`へ分離する。
2. ブランチの意図と構成を確認する。
   - PR説明、コミットメッセージ、関連ドキュメントから機能全体の目的と対象外を整理する。
   - コミット間の依存、重複、取りこぼし、不要なmerge、無関係な変更を確認する。
   - 詳細な判定基準は[Readiness確認](references/readiness-checks.md)を読む。
3. 累積結果をレビューする。
   - 最終的なコード、resource、datagen、設定、依存、テスト、ドキュメントが互いに整合するか確認する。
   - backport では `backport-1-20-1-forge` の検証結果、移植元 SHA と Forge 固有補正のコミット境界を確認する。
   - client由来の入力でserver側の状態を変更する機能では`review-client-server-authority`を使用し、許容する挙動と拒否すべき境界のテストを確認する。
4. PRの外部状態を確認する。
   - PRが存在し、GitHubへのread-onlyアクセスが利用できる場合は、checks、review、conversation、Codex Cloudの結果を取得する。
   - 取得できない状態は成功と推測せず、未確認として扱う。
   - 対象base branchの`AGENTS.md`、workflow、rulesetで要求されるcheckとレビューを正とする。
   - 外部レビューの優先度はそのまま引き継がず、具体的な実害とローカルの重大度基準から再評価する。
   - CI失敗やレビュー指摘の修正は別の実装作業として扱い、利用できないSkillを必須にしない。
5. Readinessを判定する。
   - `Ready`: P0～P2の実装Findingがなく、必須検証とレビューが確認済み。P3相当の運用・文書Noteだけでは判定を下げない。
   - `Conditional`: コード上のblockerはないが、CIや人間レビューなど外部状態が未完了。
   - `Not ready`: P0～P2の実装Finding、未コミット差分、必須検証失敗、または実害の根拠がある未解決指摘がある。
6. 結果だけを返す。
   - 重大度順の実装Findings、検証・レビュー状態、readiness、次に必要な作業を示す。
   - 運用・文書上の不足を報告する必要がある場合は独立したNotesへ分離し、実装Findingsの件数や出力枠を消費させない。
   - このSkillでは結果の報告だけを行う。修正は別の実装作業、commitは人間確認後の明示指示に従う。リモートへの書き込みは明示依頼があっても実行しない。

## 出力

1. Readiness判定
2. 実装Findings
3. CI・レビュー・検証状況
4. 運用・文書Notes（必要な場合のみ）
5. 次に必要な作業
