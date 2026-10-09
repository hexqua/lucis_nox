# `main` の PR CI と保護設定

Minecraft 1.21.1 / NeoForge の `main` に対する PR は、`.github/workflows/pr-ci.yml` の `build` と `gametest` で検証する。この文書は人間が GitHub 上で設定するための導入手順であり、保護設定の適用済み状態を示さない。Codex はローカルの準備と読み取りだけを行い、PR 作成・設定変更・Actions の手動実行を含むリモートへの書き込みを行わない。

## CI の構成

- `pull_request` の `opened` / `synchronize` / `reopened` / `ready_for_review` で実行する。対象 base branch は `main` のみ。
- Java 21 で `./gradlew build` と `./gradlew runGameTestServer` を別 job として実行する。各 job は独立した GitHub-hosted runner を使う。
- action は参照元で採用されている完全な commit SHA に固定する。更新時は action の変更内容を確認し、同じ PR で wrapper validation の許可設定も見直す。
- checkout は認証情報を保持せず、`GITHUB_TOKEN` は `contents: read` とする。repository secrets、self-hosted runner、`pull_request_target` は使用しない。
- PR が持ち込む Gradle Wrapper を実行する前に wrapper validation を行う。クライアント GUI は起動しない。

## 導入順序

1. 人間が workflow を含む `main` 向け PR を作成する。この段階では新しい required check を有効化しない。
2. PR 上で `build` と `gametest` が実際に実行され、成功することを確認する。起動しない場合は Actions の許可設定、fork PR の承認待ち、workflow の構文を確認する。
3. 人間のレビュー後、workflow を `main` に取り込む。
4. `main` に workflow が存在し、成功した check run の名前を GitHub 上で確認できてから、Ruleset に `build` と `gametest` を required check として登録する。画面上では `PR CI / build`、`PR CI / gametest` と表示される場合があるため、実際の check 名を確認する。
5. 後続 PR で両 check の失敗・未完了時に merge が制限されることを人間が確認する。

workflow のない段階で required check を先に設定すると、期待する check が報告されず取り込みが進まなくなる可能性がある。

## 人間が設定する項目

- Actions の workflow permissions は読み取りを基本とし、Actions による PR 作成・承認を許可しない。
- action の許可リストを使う場合は GitHub 所有 action と、workflow に固定した `gradle/actions/wrapper-validation` を許可する。
- `main` は PR 経由の取り込みとし、削除・force push を制限する。
- required check は `build` と `gametest`。base branch の最新状態での検証も要求する。
- 通常変更は merge commit、バージョン更新だけ rebase merge を許容する。squash merge は使用しない。backport 時は merge commit を選ばず、機能を構成する個別コミットを使う。
- 古い承認の取り消しや conversation resolution は、レビュー人数・実運用に応じて設定する。

Codex Cloud 等の外部レビューを利用する場合は補助として扱う。接続やスマートトリガーの設定を、この移植の必須条件とはしない。指摘の重大度はローカルのレビュー基準で再評価し、CI と人間の判断を置き換えない。
