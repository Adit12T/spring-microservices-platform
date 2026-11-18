-- Synthetic sample users for the demo.
INSERT INTO app_user (name, birth_date) VALUES ('Ada', DATE '1990-12-10'), ('Linus', DATE '1985-12-28');
INSERT INTO post (description, created_at, user_id) VALUES ('Hello from the user service', CURRENT_TIMESTAMP, 1);
