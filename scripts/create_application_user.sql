DO
$do$
    BEGIN
        IF NOT EXISTS(SELECT
                      FROM pg_roles
                      WHERE rolname = '<username>') THEN
            CREATE USER <username> WITH LOGIN;
            GRANT rds_iam TO <username>;
            GRANT CONNECT on DATABASE datashare to <username>;
        END IF;
    END
$do$;

ALTER DEFAULT PRIVILEGES FOR USER <username> IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE, REFERENCES ON TABLES TO <username>;
GRANT CREATE ON SCHEMA public TO <username>;
