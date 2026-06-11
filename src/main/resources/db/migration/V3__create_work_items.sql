CREATE TABLE work_items(
    id BIGSERIAL PRIMARY KEY,
    game_id BIGINT NOT NULL REFERENCES games(id),
    milestone_id BIGINT REFERENCES milestones(id),
    assignee_user_id BIGINT REFERENCES users(id),
    title VARCHAR(180) NOT NULL,
    description TEXT,
    status VARCHAR(40) NOT NULL,
    priority VARCHAR(40) NOT NULL,
    due_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_work_items_game_id ON work_items(game_id);
CREATE INDEX idx_work_items_milestone_id ON work_items(milestone_id);
CREATE INDEX idx_work_items_assignee_user_id ON work_items(assignee_user_id);
