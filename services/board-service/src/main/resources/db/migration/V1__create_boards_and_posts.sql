CREATE TABLE IF NOT EXISTS ${board_table} (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    visibility VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_boards_status ON ${board_table}(status);

CREATE TABLE IF NOT EXISTS ${post_table} (
    id UUID PRIMARY KEY,
    board_id UUID NOT NULL REFERENCES ${board_table}(id) ON DELETE CASCADE,
    author_email VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_posts_board ON ${post_table}(board_id);
CREATE INDEX IF NOT EXISTS idx_posts_board_status ON ${post_table}(board_id, status);
CREATE INDEX IF NOT EXISTS idx_posts_author ON ${post_table}(author_email);
