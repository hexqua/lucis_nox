# Lucis Nox

## 概要

光をテーマにした1.21.1Neoforge向け魔術MODです。

## 導入方法

- `mods`配下に`jar`を入れればOKです。
- Minecraft 1.21.1 / NeoForge 用の GeckoLib、Patchouli、Curios が必要です。対応バージョンは [`gradle.properties`](gradle.properties) と MOD の依存定義を参照してください。

## 開発環境

`main` は Minecraft 1.21.1 / NeoForge / Java 21 の開発基準です。`1.20.1-main` 向けの変更は個別に選んで backport します。

Windows の PowerShell では、`JDK21_HOME` を設定するか `%USERPROFILE%\.jdks` / `%USERPROFILE%\.gradle\jdks` に JDK 21 を置いてから実行してください。

```powershell
.\scripts\use-java.ps1
./gradlew.bat build
./gradlew.bat runGameTestServer
```

[`use-java.ps1`](scripts/use-java.ps1) は現在の PowerShell の環境変数を切り替えます。別の PowerShell プロセスで起動すると元のシェルには反映されません。Java toolchain とは別に、IDE の Project SDK / Gradle JVM も Java 21 に合わせます。

スクリプトが実行ポリシーでブロックされる場合は、現在の PowerShell だけに `Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass` を適用してから再実行します。組織のポリシーで変更できない場合は、[`AGENTS.md`](AGENTS.md) の手動 `JAVA_HOME` 切替を使います。

- GameTest は `src/gameTest` を専用サーバーの `run/gametest` で実行します。クライアントの描画・操作は人間が `./gradlew.bat runClient` で確認します。
- `runGameTestServer` は起動前に `run/gametest/world` を毎回削除し、初期状態から検証します。通常の開発ワールド `run/world` は保持します。IDE の直接起動ではこの初期化は行われません。
- データ生成は `./gradlew.bat runData`、IDE 実行構成の同期は `./gradlew.bat neoForgeIdeSync` です。通常の検証では `clean` を付けません。
- `build` には UTF-8 BOM・不正な UTF-8・明らかな文字化けの検査を含みます。単独実行は `./gradlew.bat checkTextEncodingHygiene` です。
- `build` では `src/main/java` の廃止予定 API 利用と `@SuppressWarnings("removal")` をエラーとして検出します。単独実行は `./gradlew.bat checkProjectRemovalWarnings checkRemovalWarningSuppressions` です。
- 共有 IDE 設定は Inspection profile のみです。個人の SDK パス・実行構成・workspace は共有しません。
- 開発・レビュー・スキルの使い分けは [`AGENTS.md`](AGENTS.md)、PR CI と GitHub 保護設定の導入は [`docs/github-pr-protection.md`](docs/github-pr-protection.md) を参照してください。

## ライセンスや使用について

### 許可(Permissions)

- modpackにはご自由にどうぞ
- 前提modにするのもご自由にどうぞ
- リソースパックもご自由にどうぞ
- スクリーンショット、プレイ動画、動画配信も収益化含めご自由にどうぞ

### 禁止(Restrictions)

- `THIRD_PARTY_NOTICES.md`にある素材のライセンスを破るのはやめてください

### License

- Code: MIT (see `LICENSE`).
- Original assets created for this project: CC0-1.0 (free to use).
- Third-party assets: see `THIRD_PARTY_NOTICES.md` for the applicable licenses.
