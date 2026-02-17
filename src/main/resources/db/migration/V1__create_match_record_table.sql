CREATE TABLE IF NOT EXISTS match_data (
    match_id              VARCHAR(50)      NOT NULL,
    puuid                 VARCHAR(100)     NOT NULL,
    win                   BOOLEAN          NOT NULL,
    game_duration_minutes DOUBLE PRECISION NOT NULL,

    -- Combat
    kills                       INTEGER           NOT NULL,
    deaths                      INTEGER           NOT NULL,
    assists                     INTEGER           NOT NULL,
    kda                         DOUBLE  PRECISION NOT NULL,
    solo_kills                  INTEGER           NOT NULL,
    damage_per_minute           DOUBLE  PRECISION NOT NULL,
    damage_per_gold             DOUBLE  PRECISION NOT NULL,
    team_damage_percentage      DOUBLE  PRECISION NOT NULL,
    damage_taken_percentage     DOUBLE  PRECISION NOT NULL,
    kill_participation          DOUBLE  PRECISION NOT NULL,

    -- Economy
    gold_per_minute DOUBLE PRECISION NOT NULL,
    cs_per_minute   DOUBLE PRECISION NOT NULL,

    -- Objectives
    damage_to_turrets    INTEGER NOT NULL,
    damage_to_objectives INTEGER NOT NULL,
    turret_plates_taken  INTEGER NOT NULL,

    -- Vision
    vision_score_per_minute DOUBLE PRECISION NOT NULL,
    wards_placed            INTEGER          NOT NULL,
    wards_killed            INTEGER          NOT NULL,
    control_wards_placed    INTEGER          NOT NULL,

    -- Timeline (nullable)
    cs_at10   DOUBLE PRECISION,
    gold_at10 DOUBLE PRECISION,
    gold_at15 DOUBLE PRECISION,
    xp_at15   DOUBLE PRECISION,

    PRIMARY KEY (match_id, puuid)
);