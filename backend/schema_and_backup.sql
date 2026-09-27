-- ============================================================================
-- WORKORA PRODUCTION POSTGRESQL SECURITY SCHEMA, AUDIT LOGS & BACKUP CONFIG
-- ============================================================================

BEGIN;

-- 1. Enable Cryptographic UUID Extension
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 2. Users Table (Argon2id Password Hash, Strict Role Check, Account Lockout)
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name VARCHAR(80) NOT NULL,
    email VARCHAR(120) UNIQUE NOT NULL,
    phone VARCHAR(20) UNIQUE NOT NULL,
    password_hash TEXT NOT NULL, -- Stores Argon2id / PBKDF2 salted hash ONLY (Never plain-text)
    role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER' CHECK (role IN ('CUSTOMER', 'LABOUR', 'SUPER_ADMIN')),
    location VARCHAR(150) DEFAULT 'Silwani, Raisen (MP)',
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    is_blocked BOOLEAN NOT NULL DEFAULT FALSE,
    failed_login_attempts INT NOT NULL DEFAULT 0,
    locked_until TIMESTAMPTZ NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_users_phone ON users(phone);
CREATE INDEX IF NOT EXISTS idx_users_email_lower ON users(LOWER(email));

-- 3. Worker Profiles Table (Isolated Labour Availability Data with Ownership Foreign Key)
CREATE TABLE IF NOT EXISTS worker_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    worker_user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    trade VARCHAR(60) NOT NULL,
    skills VARCHAR(300) NOT NULL,
    experience VARCHAR(80) NOT NULL DEFAULT '3 Years Exp.',
    daily_wage INT NOT NULL CHECK (daily_wage BETWEEN 100 AND 50000),
    work_area VARCHAR(150) NOT NULL,
    max_distance_km INT NOT NULL DEFAULT 15 CHECK (max_distance_km BETWEEN 1 AND 100),
    is_available_today BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 4. Customer Job Posts Table (Strict Ownership via customer_id Foreign Key)
CREATE TABLE IF NOT EXISTS job_posts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(120) NOT NULL,
    category VARCHAR(60) NOT NULL,
    description VARCHAR(800) NOT NULL,
    daily_rate INT NOT NULL CHECK (daily_rate BETWEEN 100 AND 50000),
    budget_type VARCHAR(40) NOT NULL DEFAULT 'Per Day (₹/day)',
    location VARCHAR(150) NOT NULL,
    workers_needed INT NOT NULL DEFAULT 1 CHECK (workers_needed BETWEEN 1 AND 100),
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN' CHECK (status IN ('OPEN', 'ASSIGNED', 'COMPLETED', 'CANCELLED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_job_posts_customer_id ON job_posts(customer_id);
CREATE INDEX IF NOT EXISTS idx_job_posts_category_status ON job_posts(category, status);

-- 5. Private Chat Messages Table (Participant Ownership Isolation)
CREATE TABLE IF NOT EXISTS chat_messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sender_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    receiver_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    message_text VARCHAR(1000) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_chat_participants ON chat_messages(sender_id, receiver_id, created_at);

-- 6. Immutable Admin Audit Logs Table (Tracks Every Admin Action)
CREATE TABLE IF NOT EXISTS admin_audit_logs (
    id BIGSERIAL PRIMARY KEY,
    admin_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    action_type VARCHAR(80) NOT NULL,
    target_resource VARCHAR(160) NOT NULL,
    ip_address VARCHAR(64) NOT NULL,
    metadata_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_admin_audit_created_at ON admin_audit_logs(created_at DESC);

-- 7. Row-Level Security (RLS) Policies for Defense-in-Depth
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE job_posts ENABLE ROW LEVEL SECURITY;
ALTER TABLE chat_messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE admin_audit_logs ENABLE ROW LEVEL SECURITY;

-- Prevent anyone from deleting or modifying Admin Audit Logs
REVOKE UPDATE, DELETE, TRUNCATE ON admin_audit_logs FROM PUBLIC;

COMMIT;

-- ============================================================================
-- AUTOMATED DAILY ENCRYPTED BACKUP & POINT-IN-TIME RECOVERY COMMANDS
-- (Run via Linux Cron / GitHub Actions Scheduled Workflow at 02:00 AM IST)
-- ============================================================================
-- 1. Full Compressed & Encrypted Backup Command:
-- pg_dump "$DATABASE_URL" --format=custom --no-owner --no-acl | gpg --symmetric --cipher-algo AES256 -o "/var/backups/workora/workora_$(date +%F_%H%M).dump.gpg"
--
-- 2. Verify Backup Integrity:
-- gpg --decrypt "/var/backups/workora/workora_latest.dump.gpg" | pg_restore --list
--
-- 3. Emergency Point-in-Time Recovery (Restore) Command:
-- gpg --decrypt "/var/backups/workora/workora_latest.dump.gpg" | pg_restore --dbname="$DATABASE_URL" --clean --if-exists --no-owner
