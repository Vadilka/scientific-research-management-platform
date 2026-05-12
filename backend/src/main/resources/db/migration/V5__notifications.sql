create table notifications (
    id bigserial primary key,
    recipient_id bigint not null references users(id) on delete cascade,
    submission_id bigint references article_submissions(id) on delete cascade,
    notification_type varchar(40) not null,
    title varchar(200) not null,
    message varchar(1000) not null,
    read_at timestamptz,
    created_at timestamptz not null,
    updated_at timestamptz not null
);

create index idx_notifications_recipient_created on notifications(recipient_id, created_at desc);
create index idx_notifications_recipient_unread on notifications(recipient_id, read_at);
create index idx_notifications_submission on notifications(submission_id);
