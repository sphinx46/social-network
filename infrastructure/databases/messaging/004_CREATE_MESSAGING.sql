DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_database WHERE datname = 'messaging') THEN
        CREATE DATABASE messaging;
END IF;
END $$;