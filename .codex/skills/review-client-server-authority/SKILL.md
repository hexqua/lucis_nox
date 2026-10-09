---
name: review-client-server-authority
description: "Minecraft MODのclient由来入力とserver側の権威境界を読み取り専用でレビューする。C2S packet、座標、entity、slot、mode、照準結果などを使って、block設置・破壊、damage、inventory、転送、召喚その他のserver状態を変更する実装や、不正client・不正packetの指摘を評価するときに使用する。具体的な実害と突破される制約で重大度を判断し、実装修正、仕様GameTest、findingなしを区別する。"
---

# Client / Server権威レビュー

client入力を無条件に信用せず、同時にclientの表示・視線・選択結果をserverで完全再現することも一律には要求しない。実際に守るべきserver側制約と被害へ焦点を当てる。

このSkillは専門観点だけを提供する。レビュー対象と出力形式は`review-local-change`または`review-feature-branch`に従い、ファイル修正やPR操作は行わない。

## 判定手順

1. clientが制御できる入力とserver側の変更箇所を特定する。
   - packet field、decode、handler、保留データ、cast data、最終的な設置・破壊・damage・inventory更新まで追う。
2. serverが守るべき制約を列挙する。
   - 射程、dimensionとworld bounds、chunkと対象の有効性、置換・破壊可否、権限・所有権・土地保護、resource消費、cooldown、回数・対象数、影響範囲を変更内容に応じて確認する。
3. 具体的な攻撃経路を確認する。
   - 制御する値、突破される制約、serverで起きる結果、影響対象、反復可能性を示せない懸念はsecurity findingにしない。
4. 許容差の意図を確認する。
   - 要求、既存挙動、コメント、GameTestから、UXのためclient結果を採用する設計かを確認する。
   - 意図を確認できない場合は、Codexだけで仕様と認定せず、人間判断が必要な前提として報告する。
5. 対応を選ぶ。
   - 重要なserver側制約を突破する場合は実装修正を求める。
   - 意図された限定的な許容差で制約が守られているがテストがない場合は、許容動作と拒否境界を示すGameTest追加を求める。
   - 意図と両側のテストが既に明確なら、その許容差だけをfindingにしない。

## 重大度

重大度は[共通のレビュー基準](../review-local-change/references/review-criteria.md)を使用し、このSkill独自の区分へ置き換えない。

- P0・P1: 保護・権限の迂回、既存blockやdataの破壊、他playerへの重大な被害、複製、広範囲または反復可能なserver停止など、具体的で重大な実害が確認できる。
- P2: 有意な不正利益またはworld整合性の破壊が再現できるが、影響が限定的または回復可能である。
- 検証状況・残余リスク: 意図された許容差についてテストが不足しているだけで、直接の不具合や制約突破を確認できない。テスト不足だけではFindingにしない。
- Findingなし: 正規の射程、cost、cooldown、権限、対象条件を維持した限定的な照準差で、意図とテストが明確である。

改造clientや不正packetが必要という事実だけで重大度を上げ下げしない。外部レビューのP0～P3表記も自動対応させず、確認できた被害と緊急性から再評価する。

## GameTest

仕様テストで対応する場合は、成功ケースだけで疑わしい挙動を追認しない。

- 許容ケース: UX上必要なclient / server差が維持される。
- 拒否ケース: 射程外、無効座標、置換不可、権限拒否、土地保護event、resource不足など、関連する重要境界を越えられない。

視線一致を要求しない設置機能では、視線を遮った成功ケースだけでなく、射程clamp、元の射程外座標への非設置、設置権限とblock設置eventの拒否まで確認して初めて意図した仕様として扱う。

## Findingの要件

不正clientを根拠にfindingを出す場合は、次を含める。

- clientが操作する入力
- 到達するserver側処理
- 突破される具体的な制約
- 実際の被害と影響範囲
- 実装修正、仕様確認、GameTest追加のどれが必要か
