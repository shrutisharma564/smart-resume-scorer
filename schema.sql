-- =====================================================================
-- Smart Resume Scorer - Database Schema
-- =====================================================================

CREATE DATABASE IF NOT EXISTS resume_scorer_db;
USE resume_scorer_db;

-- ---------------------------------------------------------------------
-- Users : login credentials for the app
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------
-- Candidates : one row per resume added (manually or via PDF)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS candidates (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(100) NOT NULL,
    email          VARCHAR(100),
    phone          VARCHAR(20),
    cgpa           DECIMAL(4,2) NOT NULL,
    project_count  INT NOT NULL,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ---------------------------------------------------------------------
-- Skills : master list of distinct skill names (normalized, no duplicates)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS skills (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    skill_name  VARCHAR(100) NOT NULL UNIQUE
);

-- ---------------------------------------------------------------------
-- Candidate_Skills : many-to-many bridge table (candidate <-> skill)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS candidate_skills (
    candidate_id INT NOT NULL,
    skill_id     INT NOT NULL,
    PRIMARY KEY (candidate_id, skill_id),
    FOREIGN KEY (candidate_id) REFERENCES candidates(id) ON DELETE CASCADE,
    FOREIGN KEY (skill_id)     REFERENCES skills(id)     ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- Evaluations : history of every "Evaluate & Rank" run, per candidate
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS evaluations (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    candidate_id      INT NOT NULL,
    job_skills_input  VARCHAR(255) NOT NULL,
    skill_points      INT NOT NULL,
    cgpa_points       INT NOT NULL,
    project_points    INT NOT NULL,
    total_score       INT NOT NULL,
    decision          VARCHAR(20) NOT NULL,
    evaluated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (candidate_id) REFERENCES candidates(id) ON DELETE CASCADE
);

-- Helpful indexes for lookups (hash-index style behaviour on InnoDB B-tree)
CREATE INDEX idx_candidate_name   ON candidates(name);
CREATE INDEX idx_eval_candidate   ON evaluations(candidate_id);
