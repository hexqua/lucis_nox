param(
    [ValidateSet('17', '21')]
    [string]$Version = '21',
    [switch]$StopGradleDaemons,
    [switch]$SkipGradleCheck
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Get-ConfiguredJdkHome {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Version
    )

    $envName = "JDK${Version}_HOME"
    $candidates = @()

    foreach ($scope in 'Process', 'User', 'Machine') {
        $value = [Environment]::GetEnvironmentVariable($envName, $scope)
        if ([string]::IsNullOrWhiteSpace($value)) {
            continue
        }

        $candidates += [pscustomobject]@{
            Source = "${envName} (${scope})"
            Path   = $value
        }
    }

    foreach ($candidate in $candidates | Sort-Object Path -Unique) {
        if (Test-Path (Join-Path $candidate.Path 'bin\java.exe')) {
            return $candidate
        }
    }

    return $null
}

function Find-JdkHome {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Version
    )

    $roots = @()
    if (-not [string]::IsNullOrWhiteSpace($env:USERPROFILE)) {
        $roots += (Join-Path $env:USERPROFILE '.jdks')
        $roots += (Join-Path $env:USERPROFILE '.gradle\jdks')
    }

    foreach ($root in $roots) {
        if (-not (Test-Path $root)) {
            continue
        }

        $matches = Get-ChildItem -LiteralPath $root -Directory -ErrorAction SilentlyContinue |
                Where-Object {
                    $_.Name -match "(^|[-_.])${Version}([-.]|$)" -or
                            $_.Name -match "jdk.*${Version}" -or
                            $_.Name -match "${Version}.*jdk"
                } |
                Sort-Object FullName

        foreach ($match in $matches) {
            if (Test-Path (Join-Path $match.FullName 'bin\java.exe')) {
                return [pscustomobject]@{
                    Source = "auto-detect (${root})"
                    Path   = $match.FullName
                }
            }
        }
    }

    return $null
}

$selectedJdk = Get-ConfiguredJdkHome -Version $Version
if ($null -eq $selectedJdk) {
    $selectedJdk = Find-JdkHome -Version $Version
}

if ($null -eq $selectedJdk) {
    throw "JDK ${Version} was not found. Set JDK${Version}_HOME or place a JDK under `$env:USERPROFILE\\.jdks or `$env:USERPROFILE\\.gradle\\jdks."
}

$javaHome = $selectedJdk.Path
$javaBin = Join-Path $javaHome 'bin'
$pathParts = @($env:Path -split ';' | Where-Object { -not [string]::IsNullOrWhiteSpace($_) })
$env:JAVA_HOME = $javaHome
$env:Path = (@($javaBin) + ($pathParts | Where-Object { $_ -ne $javaBin }) | Select-Object -Unique) -join ';'

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot '..')

Write-Host "JAVA_HOME switched to JDK ${Version}: $javaHome"
Write-Host "Detected from: $($selectedJdk.Source)"

& (Join-Path $javaBin 'java.exe') -version

if ($StopGradleDaemons -and (Test-Path (Join-Path $repoRoot 'gradlew.bat'))) {
    Push-Location $repoRoot
    try {
        & .\gradlew.bat --stop | Out-Null
    } finally {
        Pop-Location
    }
}

if (-not $SkipGradleCheck -and (Test-Path (Join-Path $repoRoot 'gradlew.bat'))) {
    Push-Location $repoRoot
    try {
        & .\gradlew.bat --version
    } finally {
        Pop-Location
    }
}
