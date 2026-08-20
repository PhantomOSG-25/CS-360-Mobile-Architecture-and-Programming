# Architecture

## Structure

```text
UI activities and adapter
        |
        v
Repository interfaces
        |
        v
SQLiteOpenHelper database

Cross-cutting: validation, password hashing, session storage, notifications
```

## Responsibilities

- `ui`: renders screens, collects input, and coordinates background work.
- `data`: owns database schema, queries, account authentication, and record persistence.
- `model`: immutable values transferred between the data and UI layers.
- `security`: derives and verifies salted PBKDF2 password hashes.
- `util`: validation, progress calculation, session storage, and notifications.

Database work runs on a single background executor. Results are returned to the main thread before views are updated. Activities never compose raw SQL.

## Data model

`users` stores a unique case-insensitive username, password hash, salt, goal weight, and notification preference. `weights` stores a user foreign key, ISO-8601 date, and weight. A unique `(user_id, entry_date)` constraint prevents accidental duplicate daily entries. Foreign-key cascade deletion removes a user's records when the account is deleted.

## Tradeoffs

The project uses platform SQLite rather than Room to demonstrate SQL schema ownership and keep the dependency surface small. A larger application would benefit from Room migrations, dependency injection, lifecycle-aware view models, encrypted database storage, and a dedicated account recovery design.

