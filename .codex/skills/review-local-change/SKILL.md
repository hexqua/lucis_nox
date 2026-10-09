---
name: review-local-change
description: "コミット前のローカル変更を読み取り専用でレビューする。staged、unstaged、untracked差分または指定コミットについて、実装意図、設計、正しさ、回帰、影響範囲、検証不足を確認するときに使用する。base branchとの差分全体、PR readiness、CI、Codex Cloudレビューには使用せず、ファイル修正、stage、commit、pushは行わない。"
---

# ローカル変更レビュー

実装単位の差分をコミット前に検査し、根拠のあるfindingsだけを返す。

## 手順

1. 対象を固定する。
   - 指定がなければ、現在のstaged、unstaged、untracked変更を対象にする。
   - 指定コミットのレビューでは、そのコミットだけを対象にする。
   - base branchからの累積差分やPR全体が対象なら、ここで止めて`review-feature-branch`を使用する。
2. 変更意図を確認する。
   - ユーザー要求、適用される`AGENTS.md`、変更ファイル、周辺実装、既存テストを読む。
   - 作業ツリーに別目的の変更が混在する場合は、対象外差分を区別して報告する。
3. 差分と関連箇所を調べる。
   - `git status`、`git diff`、`git diff --cached`を使い、untrackedファイルも確認する。
   - 呼び出し元、登録処理、resource、設定、保存データ、server/client境界まで必要な範囲を追う。
   - 詳細な観点は[レビュー観点](references/review-criteria.md)を読む。
4. 条件付きの専門観点を適用する。
   - backport の差分はこのブランチの `backport-1-20-1-forge` の観点で Forge / Java 17 / resource と移植元の意図を確認する。
   - clientから受け取った座標、対象、slot、modeなどでserver側の状態を変更する差分では`review-client-server-authority`を使用する。
   - 日本語・中国語や文字化けが疑われる差分では`text-encoding-hygiene`を使用する。
5. 検証状況を確認する。
   - `AGENTS.md`が要求するbuild、GameTest、optional MOD検証と、変更に対応するテストの有無を確認する。
   - 結果を確認できない検証は成功と推測せず、未確認として報告する。
   - 検証未実行やテスト不足だけを実装Findingにせず、検証状況または残余リスクへ分離する。
   - レビュー判断に必要なread-only検証は実行してよいが、tracked fileを更新するtaskは実行しない。
6. Findingsを返す。
   - 実行時・ビルド時・データ上の不具合だけをFindingとし、重大度順に、場所、問題、影響、根拠、安全な修正方針を示す。
   - PR説明、リリースノート、移行ガイド、コミット構成などの運用・文書上の不足はデフォルトで報告対象外とする。ユーザーが明示的に求めた場合だけ、Findingとは別のNoteとして最後に示す。
   - 運用・文書Noteや検証状況によって、より重大な実装Findingを省略しない。
   - 安全な対応方針は実装修正に限定しない。意図された許容差で重要なserver側制約が守られている場合は、許容動作と拒否境界を固定するテスト追加を示す。
   - 問題がない場合は「実装上のfindingsなし」とし、未実行検証と残余リスクを示す。
   - このSkillでは結果の報告だけを行う。修正は別の実装作業、commitは人間確認後の明示指示に従う。リモートへの書き込みは明示依頼があっても実行しない。

## 出力

1. 実装Findings
2. 検証状況
3. 前提・残余リスク
4. 運用・文書Notes（明示依頼がある場合のみ）
