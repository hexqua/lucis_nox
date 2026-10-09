---
name: report-gametest-failure
description: "Codexがローカルで実行した`runGameTestServer`またはoptional MOD用の`runGameTestServer...`が、GameTest failure、timeout、server crash、起動・環境エラーで一度でも失敗したときに使用する。初回失敗、作業状態、追加実行、変更範囲との関係、Minecraft固有の不安定要因を観測し、後続実行が成功しても省略せず最終報告へ残す。テストや実装の修正、Issue作成、tracked fileへの個別記録、commit、push、完了可否の決定には使用しない。"
---

# GameTest失敗報告

GameTestの初回失敗を消さず、後から実装修正・テスト安定化・テスト仕様変更を判断できる観測結果を返す。単発失敗をflakyと断定しない。

## 観測手順

1. 初回失敗を保持する。
   - task、GameTest profile、test ID、失敗message、assertion・timeout・server crash・起動環境失敗の別を確認する。
   - 追加実行で上書きされる前に、command出力、`run/gametest/logs/latest.log`（Lucis Noxの通常GameTest）、必要なcrash reportから判断に必要な箇所だけを読む。
2. 作業状態を記録する。
   - HEADの短縮SHA、staged・unstaged・untrackedの有無、変更ファイル、今回の変更との関係を確認する。
   - 未コミット変更がある場合は、同一SHAではなく同じworking treeであることを比較条件にする。
3. 必要に応じて追加実行する。
   - 原因分類に有効で、時間と環境が許す場合は、sourceを変更せず同じtaskを最大2回まで追加実行する。
   - 実行ツール側の強制終了timeoutだった場合は、追加実行のtimeoutを前回の2倍以上へ延長する。前回値が不明な場合は、1.21.1では300秒、1.20.1では600秒を確保する。
   - GameTest自身が報告したtest timeoutと、実行ツール側の強制終了timeoutを区別する。前者には実行ツールのtimeout延長を原因対策として適用しない。
   - commandが実行中としてyieldされた場合は、waitで同じ実行の完了を追跡する。yieldを失敗や追加実行1回として数えない。
   - 強制的に再実行せず、未実行・中断した場合は理由を残す。
   - source変更後の成功は非再現確認に使わず、「変更後に成功」と区別する。
4. 暫定分類する。
   - `再現`: 同じ失敗が追加実行でも発生した。
   - `非再現`: sourceを変えない追加実行では発生しなかった。
   - `環境失敗`: dependency取得、wrapper、runner、起動条件など、GameTest本体に到達していない。
   - `判定不能`: 証拠または追加実行が不足している。
5. 原因候補を整理する。
   - 今回の実装変更
   - exact tick・瞬間状態
   - entity AI・物理・random
   - chunk・resource読込
   - test間の空間干渉
   - static state・config・event listener残留
   - assertionが仕様より厳密
   - optional MODまたは実行環境
   - 不明

## 報告規則

- 失敗時は作業中に短く通知し、最終報告へ全観測を再掲する。
- 同じ作業中に複数件あれば`GT-OBS-01`から連番を付ける。
- 後続実行が成功しても、GameTestを単純な「成功」とだけ報告しない。
- `非再現`はflaky確定や見送り許可を意味しない。
- 変更範囲との関連、通常プレイへの影響、作業をblockする可能性を報告するが、このSkillだけで完了可否を決めない。
- 個別観測用のtracked fileやIssueを作成しない。テストや実装も変更しない。
- 公開報告にはローカルの絶対path、account名、machine名、tokenなどを含めない。ローカル生ログをそのまま貼らず、必要な部分だけを匿名化して要約する。

## 出力

```markdown
### GameTest失敗観測 GT-OBS-01

- task / profile:
- 作業状態: HEAD、working tree、追加実行間のsource変更
- 変更範囲との関係: 関連あり / 関連なし / 不明
- 初回失敗: test ID、種別、message、関連状態
- 追加実行: 各結果、または未実行理由
- 暫定分類: 再現 / 非再現 / 環境失敗 / 判定不能
- 原因候補:
- 通常プレイへの影響:
- test設計を見直す場合の観測点:
- 検証結果を単純な成功として扱えるか:
```

Issue化やテスト変更を提案する場合も、まず観測事実と推測を分離する。次回に確認すべきtick、entity・block状態、chunk、config、event、単独実行との差など、判断材料を具体的に示す。
