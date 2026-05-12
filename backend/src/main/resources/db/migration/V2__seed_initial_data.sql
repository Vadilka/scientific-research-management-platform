-- Development/demo seed data used by the university project and local Docker profile.
-- Production deployments should replace these accounts with real user onboarding.

insert into scientific_categories (code, name, description, created_at, updated_at)
values
    ('CS', 'Computer Science', 'Articles focused on software engineering, systems, and computation.', now(), now()),
    ('AI', 'Artificial Intelligence', 'Articles related to machine learning, intelligent systems, and data-driven reasoning.', now(), now()),
    ('NET', 'Networks and Distributed Systems', 'Articles covering communication systems, cloud, and distributed architectures.', now(), now());

insert into users (full_name, email, password_hash, role_name, enabled, created_at, updated_at)
values
    ('Admin User', 'admin@san.local', '$2a$10$7EqJtq98hPqEX7fNZaFWoOHi5/6Y5KJeM3PcTJS3BM4atlTqcKoy.', 'ADMIN', true, now(), now()),
    ('Editor User', 'editor@san.local', '$2a$10$7EqJtq98hPqEX7fNZaFWoOHi5/6Y5KJeM3PcTJS3BM4atlTqcKoy.', 'EDITOR', true, now(), now()),
    ('Reviewer User', 'reviewer@san.local', '$2a$10$7EqJtq98hPqEX7fNZaFWoOHi5/6Y5KJeM3PcTJS3BM4atlTqcKoy.', 'REVIEWER', true, now(), now()),
    ('Author User', 'author@san.local', '$2a$10$7EqJtq98hPqEX7fNZaFWoOHi5/6Y5KJeM3PcTJS3BM4atlTqcKoy.', 'AUTHOR', true, now(), now());
