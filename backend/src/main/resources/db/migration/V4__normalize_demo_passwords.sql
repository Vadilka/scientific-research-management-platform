-- Development/demo password normalization.
-- This keeps all seeded local accounts on the documented password: password.

update users
set password_hash = '{noop}password',
    updated_at = now()
where email in ('admin@san.local', 'editor@san.local', 'reviewer@san.local', 'author@san.local');
