ALTER TABLE match_data
ADD COLUMN champion_name VARCHAR(50),
ADD COLUMN role VARCHAR(20);

DELETE FROM match_data
WHERE champion_name IS NULL
    OR role IS NULL;

ALTER TABLE match_data
ALTER COLUMN champion_name SET NOT NULL,
ALTER COLUMN role SET NOT NULL;