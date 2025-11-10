DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_database WHERE datname = 'feed') THEN
        CREATE DATABASE feed;
END IF;
END $$;