-- ============================================================
-- ProManage Solutions - Project Scheduling System
-- V1__init_schema.sql
-- ============================================================

-- Enum for project status
CREATE TYPE project_status AS ENUM (
    'PENDING',      -- received, not yet scheduled
    'SCHEDULED',    -- assigned to a day in the weekly schedule
    'COMPLETED',    -- finished on time
    'MISSED'        -- deadline passed without completion
);

-- Enum for schedule generation status
CREATE TYPE schedule_status AS ENUM (
    'DRAFT',        -- generated but not yet in effect
    'ACTIVE',       -- current week's schedule being followed
    'ARCHIVED'      -- past weeks
);

-- ============================================================
-- PROJECTS
-- Stores all incoming client projects
-- ============================================================
CREATE TABLE projects (
    id              SERIAL          PRIMARY KEY,
    project_code    VARCHAR(20)     NOT NULL UNIQUE,   -- e.g. PRJ-20240225-001
    title           VARCHAR(255)    NOT NULL,
    description     TEXT,
    deadline_days   INTEGER         NOT NULL CHECK (deadline_days BETWEEN 1 AND 5),
    revenue         NUMERIC(12, 2)  NOT NULL CHECK (revenue > 0),
    status          project_status  NOT NULL DEFAULT 'PENDING',
    received_on     DATE            NOT NULL,           -- must be Mon–Sat
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- ============================================================
-- WEEKLY SCHEDULES
-- One record per week, generated on Sunday
-- ============================================================
CREATE TABLE weekly_schedules (
    id              SERIAL          PRIMARY KEY,
    week_start_date DATE            NOT NULL UNIQUE,    -- always a Monday
    generated_on    DATE            NOT NULL,           -- must be a Sunday
    status          schedule_status NOT NULL DEFAULT 'DRAFT',
    total_revenue   NUMERIC(12, 2)  NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP       NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_generated_on_sunday
        CHECK (EXTRACT(DOW FROM generated_on) = 0),     -- 0 = Sunday in PostgreSQL
    CONSTRAINT chk_week_start_monday
        CHECK (EXTRACT(DOW FROM week_start_date) = 1)   -- 1 = Monday
);

-- ============================================================
-- SCHEDULE ENTRIES
-- Maps projects to specific days within a weekly schedule
-- One project per day, max 5 per week
-- ============================================================
CREATE TABLE schedule_entries (
    id                  SERIAL      PRIMARY KEY,
    weekly_schedule_id  INTEGER     NOT NULL REFERENCES weekly_schedules(id) ON DELETE CASCADE,
    project_id          INTEGER     NOT NULL REFERENCES projects(id),
    scheduled_date      DATE        NOT NULL,           -- Mon–Fri of that week
    day_slot            INTEGER     NOT NULL CHECK (day_slot BETWEEN 1 AND 5), -- 1=Mon ... 5=Fri
    created_at          TIMESTAMP   NOT NULL DEFAULT NOW(),

    -- One project per day in a schedule
    CONSTRAINT uq_schedule_date    UNIQUE (weekly_schedule_id, scheduled_date),
    -- One slot per day
    CONSTRAINT uq_schedule_slot    UNIQUE (weekly_schedule_id, day_slot),
    -- A project can only be scheduled once
    CONSTRAINT uq_project_once     UNIQUE (project_id),

    CONSTRAINT chk_scheduled_weekday
        CHECK (EXTRACT(DOW FROM scheduled_date) BETWEEN 1 AND 5)
);

-- ============================================================
-- PREDICTIVE ANALYSIS SNAPSHOTS
-- Stores weekly revenue predictions and scheduling metrics
-- for trend analysis over time
-- ============================================================
CREATE TABLE schedule_analytics (
    id                      SERIAL          PRIMARY KEY,
    weekly_schedule_id      INTEGER         NOT NULL UNIQUE REFERENCES weekly_schedules(id),
    projects_considered     INTEGER         NOT NULL,   -- total PENDING projects available
    projects_scheduled      INTEGER         NOT NULL,   -- how many made the cut (max 5)
    projects_dropped        INTEGER         NOT NULL,   -- eligible but not selected
    projected_revenue       NUMERIC(12, 2)  NOT NULL,   -- revenue if all scheduled complete
    missed_revenue          NUMERIC(12, 2)  NOT NULL,   -- revenue lost from dropped projects
    avg_revenue_per_project NUMERIC(12, 2)  NOT NULL,
    scheduling_efficiency   NUMERIC(5, 2),              -- % of max possible revenue captured
    created_at              TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX idx_projects_status          ON projects(status);
CREATE INDEX idx_projects_received_on     ON projects(received_on);
CREATE INDEX idx_projects_deadline_days   ON projects(deadline_days);
CREATE INDEX idx_schedule_entries_week    ON schedule_entries(weekly_schedule_id);
CREATE INDEX idx_weekly_schedules_status  ON weekly_schedules(status);

-- ============================================================
-- Auto-update updated_at on row changes
-- ============================================================
CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_projects_updated_at
    BEFORE UPDATE ON projects
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER trg_weekly_schedules_updated_at
    BEFORE UPDATE ON weekly_schedules
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();
