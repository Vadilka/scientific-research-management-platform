update users
set password_hash = '{noop}password',
    updated_at = now()
where email in ('admin@san.local', 'editor@san.local', 'reviewer@san.local', 'author@san.local');
