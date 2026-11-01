CREATE EXTENSION IF NOT EXISTS vector@@

DO $$
BEGIN
    CREATE TYPE file_type_enum AS ENUM ('SOURCE', 'INFRA', 'OTHER');
EXCEPTION
    WHEN duplicate_object THEN NULL;
END $$@@

DO $$
BEGIN
    CREATE TYPE analysis_status_enum AS ENUM ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'FAILED', 'CANCELLED');
EXCEPTION
    WHEN duplicate_object THEN NULL;
END $$@@

DO $$
BEGIN
    CREATE TYPE severity_enum AS ENUM ('CRITICAL', 'HIGH', 'MEDIUM', 'LOW');
EXCEPTION
    WHEN duplicate_object THEN NULL;
END $$@@

DO $$
BEGIN
    CREATE TYPE reference_type_enum AS ENUM ('OWASP', 'CWE', 'OTHER');
EXCEPTION
    WHEN duplicate_object THEN NULL;
END $$@@

ALTER TABLE repositories
    ADD COLUMN IF NOT EXISTS owner VARCHAR(255)@@

UPDATE repositories
SET owner = split_part(regexp_replace(github_link, '\.git$', ''), '/', 4)
WHERE owner IS NULL@@

ALTER TABLE repositories
    ALTER COLUMN owner SET NOT NULL@@

ALTER TABLE repositories
    ADD COLUMN IF NOT EXISTS line_count BIGINT@@

UPDATE repositories
SET line_count = 0
WHERE line_count IS NULL@@

ALTER TABLE repositories
    ALTER COLUMN line_count SET DEFAULT 0@@

ALTER TABLE repositories
    ALTER COLUMN line_count SET NOT NULL@@

ALTER TABLE vulnerabilities
    ADD COLUMN IF NOT EXISTS cwe_id VARCHAR(30)@@

-- NULL means code statistics have not been supplied by the analysis server.
ALTER TABLE repositories
    ADD COLUMN IF NOT EXISTS languages JSONB@@

-- Each login maintains its own rotating refresh token.
CREATE TABLE IF NOT EXISTS login_sessions (
    session_id UUID PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    refresh_token_hash VARCHAR(128) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL
)@@

CREATE INDEX IF NOT EXISTS idx_login_sessions_user_id ON login_sessions(user_id)@@
CREATE INDEX IF NOT EXISTS idx_login_sessions_expires_at ON login_sessions(expires_at)@@
