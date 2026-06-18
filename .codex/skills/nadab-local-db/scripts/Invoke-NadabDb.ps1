[CmdletBinding(DefaultParameterSetName = 'Sql')]
param(
    [Parameter(Mandatory, ParameterSetName = 'Sql')]
    [string]$Sql,

    [Parameter(Mandatory, ParameterSetName = 'File')]
    [string]$File,

    [switch]$AllowWrite
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..\..\..')).Path
$envPath = Join-Path $repoRoot '.env'
if (-not (Test-Path -LiteralPath $envPath -PathType Leaf)) {
    throw "Nadab .env file not found at the repository root."
}

$dbConfig = @{}
foreach ($line in Get-Content -LiteralPath $envPath) {
    if ($line -match '^\s*(DB_URL|DB_USERNAME|DB_PASSWORD)\s*=\s*(.*?)\s*$') {
        $value = $Matches[2]
        if ($value.Length -ge 2 -and (($value.StartsWith('"') -and $value.EndsWith('"')) -or ($value.StartsWith("'") -and $value.EndsWith("'")))) {
            $value = $value.Substring(1, $value.Length - 2)
        }
        $dbConfig[$Matches[1]] = $value
    }
}

foreach ($requiredKey in 'DB_URL', 'DB_USERNAME', 'DB_PASSWORD') {
    if (-not $dbConfig.ContainsKey($requiredKey) -or [string]::IsNullOrWhiteSpace($dbConfig[$requiredKey])) {
        throw "Required setting $requiredKey is missing from .env."
    }
}

if ($dbConfig.DB_URL -notmatch '^jdbc:postgresql://(?<host>\[[^\]]+\]|[^:/]+)(?::(?<port>\d+))?/(?<database>[^?]+)(?:\?.*)?$') {
    throw 'DB_URL must be a PostgreSQL JDBC URL.'
}

$dbHost = $Matches.host.Trim('[', ']')
$dbPort = if ($Matches.port) { $Matches.port } else { '5432' }
$dbName = $Matches.database
if ($dbHost -notin @('localhost', '127.0.0.1', '::1') -or $dbName -ne 'nadab') {
    throw "Refusing non-local or non-nadab database target: $dbHost/$dbName"
}

$psqlCommand = Get-Command psql -ErrorAction SilentlyContinue
if ($psqlCommand) {
    $psqlPath = $psqlCommand.Source
} else {
    $psqlPath = Get-ChildItem 'C:\Program Files\PostgreSQL\*\bin\psql.exe' -ErrorAction SilentlyContinue |
        Sort-Object FullName -Descending |
        Select-Object -First 1 -ExpandProperty FullName
}
if (-not $psqlPath) {
    throw 'psql was not found on PATH or in a standard PostgreSQL installation directory.'
}

$arguments = @(
    '--no-psqlrc',
    '--set', 'ON_ERROR_STOP=1',
    '--host', $dbHost,
    '--port', $dbPort,
    '--username', $dbConfig.DB_USERNAME,
    '--dbname', $dbName
)

if ($PSCmdlet.ParameterSetName -eq 'Sql') {
    $arguments += @('--command', $Sql)
} else {
    $sqlFile = (Resolve-Path -LiteralPath $File).Path
    $arguments += @('--file', $sqlFile)
}

$previousPassword = $env:PGPASSWORD
$previousOptions = $env:PGOPTIONS
try {
    $env:PGPASSWORD = $dbConfig.DB_PASSWORD
    $env:PGOPTIONS = if ($AllowWrite) {
        '-c statement_timeout=15000'
    } else {
        '-c default_transaction_read_only=on -c statement_timeout=15000'
    }

    & $psqlPath @arguments
    if ($LASTEXITCODE -ne 0) {
        throw "psql exited with code $LASTEXITCODE."
    }
} finally {
    $env:PGPASSWORD = $previousPassword
    $env:PGOPTIONS = $previousOptions
}
