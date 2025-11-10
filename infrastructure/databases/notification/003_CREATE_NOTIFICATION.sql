DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_database WHERE datname = 'notification') THEN
        CREATE DATABASE notification;
END IF;
END $$;