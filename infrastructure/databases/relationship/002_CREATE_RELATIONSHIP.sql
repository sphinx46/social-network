DO $$
BEGIN
     IF NOT EXISTS (SELECT FROM pg_database WHERE datname = 'relationship') THEN
        CREATE DATABASE relationship;
END IF;
END $$;