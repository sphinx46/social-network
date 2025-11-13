DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_database WHERE datname = 'user_profile') THEN
        CREATE DATABASE user_profile;
    END IF;
END $$;