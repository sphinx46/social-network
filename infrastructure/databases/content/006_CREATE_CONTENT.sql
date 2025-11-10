DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_database WHERE datname = 'content') THEN
        CREATE DATABASE content;
END IF;
END $$;