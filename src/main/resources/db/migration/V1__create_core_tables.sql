CREATE TABLE studios (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE games (
    id BIGSERIAL PRIMARY KEY,
    studio_id BIGINT NOT NULL REFERENCES studios(id),
    title VARCHAR(160) NOT NULL,
    short_pitch TEXT,
    genre VARCHAR(80),
    current_stage VARCHAR(40) NOT NULL,
    validation_status VARCHAR(40) NOT NULL,
    target_platforms VARCHAR(240),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE game_jams (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    host VARCHAR(160),
    theme VARCHAR(160),
    start_date DATE,
    end_date DATE,
    url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE prototypes (
    id BIGSERIAL PRIMARY KEY,
    game_id BIGINT NOT NULL REFERENCES games(id),
    game_jam_id BIGINT REFERENCES game_jams(id),
    name VARCHAR(160) NOT NULL,
    build_version VARCHAR(80),
    itch_url VARCHAR(500),
    repository_url VARCHAR(500),
    playable_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE traction_snapshots (
    id BIGSERIAL PRIMARY KEY,
    game_id BIGINT NOT NULL REFERENCES games(id),
    prototype_id BIGINT REFERENCES prototypes(id),
    source VARCHAR(40) NOT NULL,
    views INTEGER NOT NULL DEFAULT 0,
    downloads INTEGER NOT NULL DEFAULT 0,
    plays INTEGER NOT NULL DEFAULT 0,
    ratings_count INTEGER NOT NULL DEFAULT 0,
    average_rating NUMERIC(3, 2),
    comments_count INTEGER NOT NULL DEFAULT 0,
    followers_gained INTEGER NOT NULL DEFAULT 0,
    wishlists INTEGER NOT NULL DEFAULT 0,
    revenue_cents INTEGER NOT NULL DEFAULT 0,
    captured_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE validation_decisions (
    id BIGSERIAL PRIMARY KEY,
    game_id BIGINT NOT NULL REFERENCES games(id),
    decision VARCHAR(40) NOT NULL,
    reason TEXT,
    decided_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE milestones (
    id BIGSERIAL PRIMARY KEY,
    game_id BIGINT NOT NULL REFERENCES games(id),
    name VARCHAR(120) NOT NULL,
    due_date DATE,
    status VARCHAR(40) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE release_checklist_items (
    id BIGSERIAL PRIMARY KEY,
    game_id BIGINT NOT NULL REFERENCES games(id),
    title VARCHAR(160) NOT NULL,
    description TEXT,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    blocks_release BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE playtests (
    id BIGSERIAL PRIMARY KEY,
    game_id BIGINT NOT NULL REFERENCES games(id),
    session_date DATE NOT NULL,
    tester_group VARCHAR(160),
    build_version VARCHAR(80),
    notes TEXT,
    main_findings TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE marketing_activities (
    id BIGSERIAL PRIMARY KEY,
    game_id BIGINT NOT NULL REFERENCES games(id),
    activity_type VARCHAR(40) NOT NULL,
    channel VARCHAR(80) NOT NULL,
    title VARCHAR(160) NOT NULL,
    scheduled_for TIMESTAMP,
    completed_at TIMESTAMP,
    result_notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
