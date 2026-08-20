# Security and privacy

## Protections included

- No network or SMS permission is declared.
- Android backup is disabled to reduce unintended health-data copying.
- Only the launcher activity is exported.
- Passwords use a random 16-byte salt and PBKDF2-HMAC-SHA256 with 120,000 iterations.
- SQL statements use bound parameters; usernames are unique without regard to case.
- Records are always queried and modified through the signed-in user ID.
- Notification permission is optional and requested only when the user enables notifications.

## Threat model and limitations

The app is designed for a single trusted Android device. It does not defend against a rooted device, a compromised operating system, memory inspection, or an attacker who already controls the unlocked user session. The SQLite database is not independently encrypted. There is no remote password recovery because there is no server and no email collection.

Do not use this educational application for clinical decisions or highly sensitive health records.

