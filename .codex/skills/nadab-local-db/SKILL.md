---
name: nadab-local-db
description: Safely connect to, inspect, and query the Nadab project's local PostgreSQL database using the local profile's .env configuration. Use when Codex needs to check local Nadab data, inspect tables or constraints, diagnose persistence issues, verify migrations, or run explicitly requested local SQL changes.
---

# Nadab Local DB

Operate from the Nadab repository root. Treat the database as local development data, never as production.

## Connect

Use the bundled PowerShell wrapper. It reads only `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` from the repository `.env`, finds `psql` on PATH or under `C:\Program Files\PostgreSQL`, and does not print credentials.

```powershell
& .\.codex\skills\nadab-local-db\scripts\Invoke-NadabDb.ps1 -Sql "SELECT current_database(), current_user;"
```

Run a SQL file with `-File`:

```powershell
& .\.codex\skills\nadab-local-db\scripts\Invoke-NadabDb.ps1 -File ".\query.sql"
```

## Workflow

1. Confirm the target reports database `nadab` before investigating data.
2. Inspect structure with PostgreSQL catalogs or `information_schema`; consult `src/main/resources/db/migration` when migration history matters.
3. Select only columns and rows needed for the task. Add a reasonable `LIMIT` to exploratory queries.
4. Prefer aggregates, identifiers, and redacted samples over dumping user content or authentication data.
5. Summarize findings without exposing credentials, tokens, email addresses, private report content, or other unnecessary personal data.

Useful inspection queries:

```sql
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
ORDER BY table_name;
```

```sql
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_schema = 'public' AND table_name = 'users'
ORDER BY ordinal_position;
```

## Write Safety

The wrapper enables PostgreSQL read-only transactions by default. Keep this default for inspection and diagnosis.

Use `-AllowWrite` only when the user explicitly requests a local data or schema change. Before running it:

1. State the exact database and rows or objects affected.
2. Add a restrictive `WHERE` clause for updates and deletes, and inspect matching rows first.
3. Prefer a committed Flyway migration for persistent schema changes; do not use ad hoc DDL as a substitute.
4. Run the smallest statement that fulfills the request and verify the result afterward.

```powershell
& .\.codex\skills\nadab-local-db\scripts\Invoke-NadabDb.ps1 -Sql "UPDATE ..." -AllowWrite
```

Never use `DROP DATABASE`, broad destructive statements, or production/dev profile credentials through this skill.

## Troubleshooting

- If `.env` is missing or incomplete, stop and report which variable name is missing without requesting or displaying its value.
- If connection fails, check that PostgreSQL is running and that `localhost:5432` is reachable. Do not silently switch to another database.
- If `psql` is not found, install PostgreSQL command-line tools or add its `bin` directory to PATH; the wrapper already checks standard Windows install locations.
- If a query times out, narrow it or inspect indexes. Do not remove the statement timeout merely to force an exploratory query through.
