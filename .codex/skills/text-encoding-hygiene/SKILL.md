---
name: text-encoding-hygiene
description: Prevent and diagnose UTF-8 mojibake in this repository. Use when Codex sees garbled Japanese text, edits Japanese comments, translations, Markdown, or resources, works from Windows PowerShell, changes AGENTS.md or other skills, or validates text encoding before committing.
---

# Text Encoding Hygiene

## Purpose

Use this skill before editing Japanese text, after seeing garbled output, or before committing changes that touched comments, translations, Markdown, JSON resources, Gradle files, or skill files. This skill is intentionally written in English so it remains readable when Japanese instructions are affected by display mojibake.

## PowerShell Reading Rules

- Treat Windows PowerShell 5.1 default text I/O as unsafe for Japanese UTF-8 files.
- Before reading Japanese text in PowerShell, set:

```powershell
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new($false)
```

- Prefer one of these explicit UTF-8 reads:

```powershell
Get-Content -Encoding UTF8 path\to\file.md
[System.IO.File]::ReadAllText($path, [System.Text.Encoding]::UTF8)
```

- If output shows garbled Japanese, stop editing that file. Re-read it with explicit UTF-8 and confirm the text is readable before making changes.
- If a Python validation script fails while reading Japanese files under the local default encoding, set `PYTHONUTF8=1` for that command.

## Writing Rules

- Do not use default `Set-Content` or `Out-File` in Windows PowerShell 5.1 for repository text files.
- Prefer Codex file editing tools for source changes.
- If shell writing is unavoidable, write UTF-8 without BOM explicitly:

```powershell
$utf8NoBom = [System.Text.UTF8Encoding]::new($false)
[System.IO.File]::WriteAllText($path, $text, $utf8NoBom)
```

- Keep edits narrow. Avoid whole-file rewrites when only a few lines need to change.

## Required Checks

- Run `git diff --name-status` and confirm the changed files match the request.
- Inspect `git diff` for unintended deletion of Japanese comments or documentation.
- For code, resource, dependency, or datagen changes, run:

```powershell
.\scripts\use-java.ps1 -Version 17
./gradlew.bat build
```

- For a faster encoding-only check, run:

```powershell
./gradlew.bat checkTextEncodingHygiene
```

## Mojibake Handling

- The Gradle check catches only obvious cases to avoid false positives.
- This repository has no path exclusions for the mojibake heuristic. Legitimate translation text may collide with simple CJK heuristics; confirm the actual bytes and text before changing a check.
- If the Gradle task reports a file, re-open that file as UTF-8 and fix the source text rather than suppressing the check.
- Do not add broad exclusions. Add a narrow path exclusion only when a legitimate file is demonstrably unavoidable.
