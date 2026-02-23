CREATE TABLE IF NOT EXISTS benchmark (
    tier                        VARCHAR(20)      NOT NULL,
    role                        VARCHAR(10)      NOT NULL,

    -- Median (primary benchmark)
    median_cs_per_min           DOUBLE PRECISION NOT NULL,
    median_kda                  DOUBLE PRECISION NOT NULL,
    median_gold_per_min         DOUBLE PRECISION NOT NULL,
    median_dpm                  DOUBLE PRECISION NOT NULL,
    median_vision_score_per_min DOUBLE PRECISION NOT NULL,
    median_kill_participation   DOUBLE PRECISION NOT NULL,

    -- Mean (secondary reference)
    avg_cs_per_min              DOUBLE PRECISION NOT NULL,
    avg_kda                     DOUBLE PRECISION NOT NULL,
    avg_gold_per_min            DOUBLE PRECISION NOT NULL,
    avg_dpm                     DOUBLE PRECISION NOT NULL,
    avg_vision_score_per_min    DOUBLE PRECISION NOT NULL,
    avg_kill_participation      DOUBLE PRECISION NOT NULL,

    PRIMARY KEY (tier, role)
);