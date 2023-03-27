DO
$do$
    BEGIN
        IF NOT EXISTS(SELECT
                      FROM pg_roles
                      WHERE rolname = '<username>') THEN
            CREATE USER <username> WITH LOGIN;
            GRANT rds_iam TO <username>;
            GRANT CONNECT on DATABASE <db_name> to <username>;
        END IF;
    END
$do$;

ALTER DEFAULT PRIVILEGES FOR USER <username> IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE, REFERENCES ON TABLES TO <username>;
ALTER DEFAULT PRIVILEGES FOR USER <username> IN SCHEMA public GRANT USAGE, SELECT, UPDATE ON SEQUENCES TO <username>;

GRANT SELECT, INSERT, UPDATE, DELETE, REFERENCES ON ALL TABLES IN SCHEMA public TO <username>;
GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA public TO <username>;

GRANT CREATE ON SCHEMA public TO <username>;
