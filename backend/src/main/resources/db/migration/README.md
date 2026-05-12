# Flyway migrations

The migration files are treated as immutable after they have been applied to a database. Even a comment-only edit changes the Flyway checksum and can break existing local Docker volumes.

- `V2__seed_initial_data.sql` contains development/demo categories and user accounts for the university project and local Docker profile.
- `V4__normalize_demo_passwords.sql` normalizes seeded local demo accounts to the documented password: `password`.

Production deployments should replace demo accounts with a real onboarding process and environment-specific secrets.
