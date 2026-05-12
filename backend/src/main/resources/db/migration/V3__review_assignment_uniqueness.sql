alter table review_assignments
    add constraint uq_review_assignments_submission_reviewer unique (submission_id, reviewer_id);
