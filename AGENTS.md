# AGENTS.md

このファイルは、このリポジトリで作業する人間/AIエージェント向けの共通ルールを定義します。

## 0. 言語ポリシー
- 本プロジェクトでのやり取り、ドキュメント、レビューコメントは原則として日本語を使用する。
- ファイルをコミットする際のコミットメッセージは日本語で記述する。
- 外部資料が英語の場合は、日本語で要点を補足する。
- ツール仕様などで英語が必須の箇所のみ、必要最小限で英語を使用する。
- プレイヤー向けテキストは `lang` による言語選択を優先する。言語選択できない設定コメントや公開 Issue フォームは英語を使用する。
- GameTest の Assert、例外、ログなどの診断文字列は検索性を保つため英語を使用し、意図を説明するコメントは日本語で記述する。

## 1. 目的
- プロジェクトの目的: Lucis Nox を開発し、スタンドアロンの魔術 MOD として独自コンテンツを提供する。
- AGENTS.md を置く目的: 人間/AI エージェントの作業手順、品質基準、言語ポリシー（原則日本語）を統一し、再現性のある開発を行う。
- 技術スタックやバージョンなどの実装条件は「2. 開発環境」に記載する。

## 2. 開発環境
- 開発対象: Minecraft 1.20.1（`1.20.1-main` とその作業ブランチ）
- Mod ローダー: Forge 47.4.10
- 言語/実行環境: Java 17
- ビルドツール: Gradle Wrapper（`./gradlew` / `./gradlew.bat`）
- 必須依存 MOD: GeckoLib、Patchouli、Curios。バージョンは `gradle.properties` と `src/main/templates/META-INF/mods.toml` を正とする。
- セットアップ手順:
1. 64bit の Java 17 をインストールし、`java -version` で確認する。
2. 既定の Java が 17 以外の場合は、ビルド実行前に一時的に `JAVA_HOME` を切り替える。
3. `./gradlew.bat --version` を実行し、JVM が Java 17 であることを確認する。
4. 必要に応じて IDE の Gradle プロジェクト再読み込みを実施する。
- ローカルでは `JDK17_HOME` を設定するか、`%USERPROFILE%\.jdks` / `%USERPROFILE%\.gradle\jdks` に JDK 17 を置き、`.\scripts\use-java.ps1` で現在の PowerShell の `JAVA_HOME` と `PATH` を切り替えられる。
- Java toolchain の指定と Gradle Wrapper 自体の JVM は別である。IDE の Project SDK / Gradle JVM も Java 17 にそろえる。

## 3. 実行コマンド
- PowerShell で Java 17 を一時適用（必要な場合）:
```powershell
# 必須: <<REPLACE_WITH_YOUR_JDK17_PATH>> を実際の JDK 17 パスに置換する
$env:JAVA_HOME='<<REPLACE_WITH_YOUR_JDK17_PATH>>'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
java -version
```
- ビルド（通常確認）:
```powershell
./gradlew.bat build
```
- クリーンビルド（必要時のみ）:
```powershell
./gradlew.bat clean build
```
- jar 出力確認:
```powershell
Get-ChildItem build\libs\*.jar
```
- 想定出力先:
  `build\libs\<mod_id>-<mod_version>+mc1.20.1.jar`
- 起動（開発クライアント）:
```powershell
./gradlew.bat runClient
```
- 注記: `runClient` は GUI（Minecraft クライアント）を起動するため、CI やヘッドレス環境では実行しない。
- 注記: 通常のビルド確認では `clean` を付けない。`clean` 実行後は開発実行環境の再生成や IDE 再同期が必要になる場合がある。
- 注記: 本プロジェクトでは Gradle Wrapper の実行はパス経由を前提にしないため、`./gradlew.bat` を使用する。
- Lint/Format:
  `./gradlew.bat checkTextEncodingHygiene` で BOM・不正な UTF-8・明らかな文字化けを検査する。`build` の `check` にも含む。専用 Format タスクは未設定。
- 自動テスト: `./gradlew.bat runGameTestServer`。`src/gameTest` のテストを専用サーバーで実行する。実装変更時は `build` と併せて確認する。
- データ生成: `./gradlew.bat runData`。生成結果を正とし、`src/generated/resources` の差分を確認して実装と一緒に扱う。
- 作業台レシピ: `datagen/RecipeGenerator.java` の `buildRecipes` に `ShapedRecipeBuilder` / `ShapelessRecipeBuilder` で追加し、`lucisnox` 名前空間で保存する。
- GameTest は `run/gametest` を使う。同じ実行ディレクトリを使う Gradle / server プロセスは直列実行し、yield は終了と扱わず同じ実行の完了を追跡する。
- `runGameTestServer` は `cleanGameTestServerWorld` により `run/gametest/world` を毎回削除してから起動する。通常の手動確認用 `run/world` は削除しない。IDE から Minecraft の実行構成を直接起動するだけでは、この Gradle の初期化処理は実行されない。
- GameTest が一度でも失敗した場合は `.codex/skills/report-gametest-failure` を使い、後続の成功だけで初回失敗を省略しない。
- `src/generated/resources/.cache` は Git 管理外であり、branch 切替や手動コピーで入った旧 JSON は `runData` だけでは消えない場合がある。削除・改名・移設では旧出力と build 出力側の残存も確認する。
- IDE 実行構成の同期には `./gradlew.bat genIntellijRuns` を使う。共有する IDE 設定は `.idea/inspectionProfiles/Project_Default.xml` に限定し、個人の SDK パスや workspace は追跡しない。

### クライアント検証の分担
- 通常の自動検証は `build`、必要な `runGameTestServer`、`runData`、静的解析・IDE Inspection とする。
- `runClient` の起動・GUI 自動操作は通常の自動検証には含めず、人間が表示・操作を確認する。ユーザーから明示依頼がある場合はその範囲で実施する。
- client の表示・入力・描画・アニメーション・音声・起動に影響する場合は `.codex/skills/report-client-verification` を使い、必要な確認シナリオと確認状態を最終報告へ引き継ぐ。
- 自動検証の成功だけで client を確認済みとしない。人間確認結果が未把握という理由だけで自動操作や追加作業を開始しない。

## 4. コーディング規約
- 命名規則: クラス/インターフェースは `PascalCase`、メソッド/フィールド/ローカル変数は `camelCase`、定数は `UPPER_SNAKE_CASE` を使用する。
- Java の型参照は import と単純クラス名を使用する。名前衝突を解消できない箇所だけ完全修飾名を使い、変更した Java ファイルの未使用 import を整理する。無関係な一括整形は行わない。
- 不要な完全修飾名は IDEA の `UnnecessaryFullyQualifiedName` Inspection で `ERROR` として確認する。Gradle build による強制は行わない。
- `@Deprecated(forRemoval = true)` API の利用は `checkProjectRemovalWarnings` でプロジェクトの `src/main/java` を再コンパイルしてエラーにする。通常の deprecated API はこの検査のエラー化対象に含めない。
- `@SuppressWarnings("removal")` で廃止予定 API の警告を隠さない。`checkRemovalWarningSuppressions` で検査し、両タスクを `build` の `check` に含める。
- 命名規則: レジストリ名・リソース ID・JSON ファイル名は `snake_case` を使用し、`lucisnox` 名前空間を前提にする。
- 設計方針: 追加要素の登録処理は既存の `registry` パッケージ構成に合わせ、初期化時に一元登録する。
- 設計方針: データ駆動で表現できる内容は `src/generated/resources` と datagen を優先し、ハードコードを最小化する。
- コメント方針: コメントは「何をしているか」より「なぜそうするか（意図・理由・制約）」を優先して記載する。
- コメント方針: 外部 MOD 仕様への依存、ワークアラウンド、クライアント/サーバー差分、実行順依存、魔法値を扱う箇所はコメント必須とする。
- コメント方針: 複雑な条件分岐、将来の拡張を前提にした設計判断、誤用しやすい API 利用箇所にはコメント推奨とする。
- コメント方針: 自明な処理の逐語説明コメントは避ける。
- コメント方針: 実装変更時はコメントも同時に更新し、不要になったコメントは削除する。
- コメント方針: 依頼範囲外の既存コメント（説明コメント・TODO・制約メモ等）は削除/改変しない。整理目的の削除は別タスクとして事前合意を必須とする。
- コメント方針: コメント本文は原則日本語で、短く具体的に記述する。
- 文字コード方針: テキストファイルは UTF-8（BOM なし）を原則とする。UTF-8 BOM はビルド失敗の要因になるため使用しない。
- 依存関係追加の方針: 追加・更新するバージョンは `gradle.properties` に集約し、`build.gradle` から参照する。
- 依存関係追加の方針: 必須依存を追加する場合は `src/main/templates/META-INF/mods.toml` の dependency 定義も更新する。
- 依存関係追加の方針: 外部アセット/ライブラリ利用時は `THIRD_PARTY_NOTICES.md` の追記要否を必ず確認する。

## 5. 変更フロー
1. 変更内容を 1〜2 文で決める（何を、なぜ変えるか）。
   - 共通機能・不具合修正は原則として `main` で開発し、非 merge の個別 SHA を選んで backport する。1.20.1 / Forge 固有の修正はこのブランチで扱う。
2. 実装する。
3. 差分確認を行い、依頼範囲外のコメント削除/改変と文字化け差分がないことを確認する。
4. `./gradlew.bat build` が成功することを確認する。実装変更では `./gradlew.bat runGameTestServer` も確認する。
5. client の動作確認が必要な場合は、上記の分担に従って人間向け確認シナリオを用意する。
6. 必要に応じて関連ドキュメントを更新する。
7. 人間への引き渡し前に `.codex/skills/review-local-change` で差分をレビューし、利用可能な JetBrains MCP の Inspection で変更ファイルを確認する。変更に起因し修正方針が明確な警告を修正し、残った警告・未確認事項を報告する。
8. 通常開発は未コミットで引き渡す。機能ブランチ全体のレビューが必要な場合は `.codex/skills/review-feature-branch` を使う。レビュー完了はコミット許可を意味しない。

## 6. レビューチェックリスト
- 必須チェック項目: Java 17 環境で `./gradlew.bat build` が成功すること。
- 必須チェック項目: 追加・変更した要素の登録漏れ（Registry/EventBus）がないこと。
- 必須チェック項目: サーバー専用環境で問題となるクライアント専用参照を追加していないこと。
- 必須チェック項目: 依頼範囲外の既存コメントが削除/改変されていないこと。
- 必須チェック項目: 日本語文字列・コメントに文字化け（例: `縺` が連続する不自然な文字列）が混入していないこと。
- リグレッション確認: 既存コンテンツの ID 変更や削除による互換性破壊を避ける。
- リグレッション確認: 依存 MOD バージョン条件を変更した場合、`src/main/templates/META-INF/mods.toml` と `gradle.properties` の整合性を確認する。

### レビューの判断基準
- 実装 Finding は実行時・ビルド時・データ上の具体的な不具合を優先する。検証未実行や文書不足だけを実装不具合とせず、検証状況・残余リスク・必要な Note として区別する。
- 外部レビューの重大度をそのまま採用せず、実害・影響範囲・再現性から `.codex/skills/review-local-change/references/review-criteria.md` の基準で判断する。
- client 入力で server の状態を変える場合は `.codex/skills/review-client-server-authority` を使い、射程・権限・所有権・対象条件・コスト等の突破を確認する。client の視線や選択結果を server で完全再現することは一律に要求しない。
- 保存済みのワールド・アイテム・プレイヤー資産と参照 content ID は後方互換または migration で保護する。権限・破壊許可・機能禁止などの管理設定も、旧設定が無視されて禁止操作が有効にならないよう保護する。
- lang・モデル・レシピ・タグ等のリソース構成や一般的なバランス・表示設定は、依頼に沿った意図した変更を許容する。旧形式の互換変換や網羅的な移行表を一律に要求せず、利用者に再設定等が必要な場合は主要な影響を申し送る。保存資産の消失や world 読込不能を起こす変更には資産保護の規則を適用する。

## 7. ドキュメント更新
- コード変更時に更新すべきファイル: `gradle.properties`（バージョン）、`build.gradle`（依存/タスク）、`src/main/templates/META-INF/mods.toml`（依存条件）、`README.md`（仕様/導入手順）、`THIRD_PARTY_NOTICES.md`（ライセンス）、`.codex/skills/**`（エージェント向け手順）。
- 更新ルール: 実装変更と同一 PR/コミット内で関連ドキュメントを更新し、差分の理由が追跡できる状態にする。
- 更新ルール: 実行手順や開発フローに影響する変更は `AGENTS.md` も同時更新する。
- CI / GitHub 保護設定 / merge 運用の変更では `docs/github-pr-protection.md` も更新する。

## 8. Git 運用
- ローカル Git 操作は必要な範囲で行えるが、通常開発の `commit` / `commit --amend` は対象差分の人間確認後、明示的なコミット指示を受けるまで行わない。
- Codex がコミットする場合、後から取り込み元と意図を追いやすい日本語コミットメッセージを書く。
- リモートへの読み取り系操作は許可する。例: `git fetch`、`git pull --ff-only`、リモート参照の確認。
- リモートへの書き込み系操作は人間のみが行う。例: `git push`、PR 作成、レビュー送信、issue/comment の投稿、remote ブランチの更新。
- Codex は明示依頼があってもリモートへ書き込まない。push、PR / Issue / コメント / Release の作成・更新、remote branch 変更、Actions の手動実行を含む。
- 明示的な backport では、完成状態を人間へ提示するための `cherry-pick -x`、競合解消後の `cherry-pick --continue`、移植先固有の補正コミットを人間確認前に作成してよい。この例外を通常開発へ適用しない。

### `1.20.1-main` の PR CI
- `.github/workflows/pr-ci.yml` は `1.20.1-main` 向け `pull_request` で `build` と `gametest` を実行する。Java 17、SHA 固定 action、wrapper validation を使用し、secrets は使わず `GITHUB_TOKEN` は read-only とする。
- 人間による `1.20.1-main` への取り込みは PR を使用する。通常変更は merge commit、バージョン更新だけ rebase merge を許容し、squash merge は使用しない。backport は merge commit ではなく個別コミットを選ぶ。
- GitHub の required check と保護設定は、workflow の導入・成功確認後に人間が設定する。詳細は `docs/github-pr-protection.md` を参照し、ローカルファイルの追加だけで設定済みと扱わない。

## 9. ブランチ間取り込み（1.21.1 -> 1.20.1）
- 基本方針: `main`（1.21.1 / NeoForge / Java 21）を主系統とし、`1.20.1-main`（1.20.1 / Forge）への反映は backport で行う。
- 基本方針: `main` と `1.20.1-main` の直接 `merge` は原則禁止とし、必要な場合は事前合意を必須とする。
- 基本方針: `merge` コミットの直接 `cherry-pick`（`git cherry-pick -m` を含む）と、擬似的なスカッシュコミットの backport は禁止とし、取り込み対象は個別コミット単位で扱う。
- 実作業では、このブランチの `.codex/skills/backport-1-20-1-forge` を使用する。初回移植と継続取り込みの区別・起点は `docs/backport-1.20.1.md` を参照する。
- AGENTS.md では次の原則だけを常設ルールとして保持する。
1. 取り込み前に対象コミットを個別 SHA で確定し、`git cherry-pick -x` を使う。
2. 1 機能を独立した連続コミット系列として保ち、無関係な整形・rename・広域整理を同じ backport 対象に混ぜない。
3. generated や resource の削除・改名・出力パス変更を含む作業では、影響ディレクトリの stale 出力混入を前提に確認する。
4. `1.20.1-main` 固有の loader/version 向け修正は原則そのブランチで閉じ、`main` への逆流は共通バグと判断できる場合だけを別コミットで扱う。
- 標準の同期方向は `main` から `1.20.1-main` のみとし、forward-port は本リポジトリの常設運用に含めない。
- 同種コンフリクトの再解決コストを下げるため、`git config rerere.enabled true` を推奨する。

## 10. Codex運用上の注意（コメント保全/文字化け対策）
- 日本語、翻訳、Markdown、resource、AGENTS.md、Skill を扱う場合は、英語で書かれた `.codex/skills/text-encoding-hygiene` を先に確認する。
- 原因整理: Windows PowerShell 5.1（コードページ 932）で `Get-Content` 既定読み取りを使うと、UTF-8日本語が文字化けして表示される。
- 対策: 日本語を含むファイルをターミナルで読む前に、`[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new($false)` を設定し、`Get-Content -Encoding UTF8` を使用する。
- 対策: PowerShell 5.1 で `Set-Content` / `Out-File` の既定エンコーディング書き込みは使わない（BOM付与や文字化け混入の原因になる）。
- 対策: シェル経由で保存が必要な場合は UTF-8 BOM なしを明示する（例: `[System.IO.File]::WriteAllText($path, $text, [System.Text.UTF8Encoding]::new($false))`）。
- 対策: 編集は必要最小限の差分に限定し、ファイル全体の再書き込みや無関係なコメント整理を行わない。
- 対策: 文字化けした表示（例: `縺` など）が出た状態では編集を続行しない。UTF-8指定で再読込して正常表示を確認してから編集する。
- 対策: 変更後は `git diff` を確認し、依頼範囲外コメントの削除と日本語の文字化け差分があれば修正してから完了とする。
- 対策: 必要に応じて `git diff | rg "^-\\s*(//|/\\*|\\*|#)"` でコメント削除行を検出し、依頼範囲内の変更かを確認する。

## 11. 禁止事項
- 事前合意なしで大規模リファクタをしない。
- 機密情報をコミットしない。
