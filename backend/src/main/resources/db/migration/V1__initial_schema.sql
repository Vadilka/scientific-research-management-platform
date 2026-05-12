create table users (
    id bigserial primary key,
    full_name varchar(150) not null,
    email varchar(180) not null unique,
    password_hash varchar(255) not null,
    role_name varchar(30) not null,
    enabled boolean not null default true,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table scientific_categories (
    id bigserial primary key,
    code varchar(50) not null unique,
    name varchar(120) not null unique,
    description varchar(500),
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table article_submissions (
    id bigserial primary key,
    title varchar(300) not null,
    abstract_text varchar(5000) not null,
    keywords varchar(1000) not null,
    corresponding_author_email varchar(180) not null,
    status varchar(40) not null,
    category_id bigint not null references scientific_categories(id),
    submitted_by_id bigint not null references users(id),
    submitted_at timestamptz,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table submission_authors (
    id bigserial primary key,
    submission_id bigint not null references article_submissions(id) on delete cascade,
    full_name varchar(150) not null,
    email varchar(180) not null,
    affiliation varchar(255) not null,
    author_order integer not null,
    corresponding_author boolean not null default false,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table submission_files (
    id bigserial primary key,
    submission_id bigint not null references article_submissions(id) on delete cascade,
    original_file_name varchar(255) not null,
    stored_file_name varchar(255) not null,
    media_type varchar(120) not null,
    file_type varchar(30) not null,
    file_size bigint not null,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table review_assignments (
    id bigserial primary key,
    submission_id bigint not null references article_submissions(id) on delete cascade,
    reviewer_id bigint not null references users(id),
    status varchar(30) not null,
    assigned_at timestamptz not null,
    due_date timestamptz,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table reviews (
    id bigserial primary key,
    assignment_id bigint not null unique references review_assignments(id) on delete cascade,
    score integer not null,
    recommendation varchar(40) not null,
    summary_comment varchar(1000) not null,
    detailed_comment varchar(5000) not null,
    submitted_at timestamptz not null,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create table editorial_decisions (
    id bigserial primary key,
    submission_id bigint not null references article_submissions(id) on delete cascade,
    editor_id bigint not null references users(id),
    decision_type varchar(20) not null,
    decision_note varchar(3000) not null,
    decided_at timestamptz not null,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create index idx_article_submissions_status on article_submissions(status);
create index idx_article_submissions_category on article_submissions(category_id);
create index idx_submission_authors_submission on submission_authors(submission_id);
create index idx_submission_files_submission on submission_files(submission_id);
create index idx_review_assignments_submission on review_assignments(submission_id);
create index idx_review_assignments_reviewer on review_assignments(reviewer_id);
create index idx_editorial_decisions_submission on editorial_decisions(submission_id);
