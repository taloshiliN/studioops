CREATE TABLE launch_plans (
    id BIGSERIAL PRIMARY KEY,
    game_id BIGINT NOT NULL UNIQUE REFERENCES games(id) ON DELETE CASCADE,
    itch_page_url VARCHAR(500),
    steam_page_url VARCHAR(500),
    demo_url VARCHAR(500),
    trailer_url VARCHAR(500),
    target_demo_date DATE,
    target_next_fest_date DATE,
    target_launch_date DATE,
    content_creator_outreach_target INTEGER NOT NULL DEFAULT 300,
    festival_submission_target INTEGER NOT NULL DEFAULT 5,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_launch_plans_creator_target
        CHECK (content_creator_outreach_target >= 0),
    CONSTRAINT chk_launch_plans_festival_target
        CHECK (festival_submission_target >= 0)
);
