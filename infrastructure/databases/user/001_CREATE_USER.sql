DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_database WHERE datname = 'user_db') THEN
        CREATE DATABASE user_db;
    END IF;
END $$;