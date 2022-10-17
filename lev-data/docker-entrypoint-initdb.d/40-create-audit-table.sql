-- The API's audit table

-- 0. Grab environment variables
\set app_user `echo "${APP_USER}"`

-- 1. Create audit table
CREATE TABLE IF NOT EXISTS lev_audit (
  id SERIAL PRIMARY KEY,
  date_time TIMESTAMP WITH TIME ZONE NOT NULL,
  username TEXT NOT NULL,
  client TEXT NOT NULL,
  uri TEXT NOT NULL,
  groups TEXT ARRAY NOT NULL,
  operation TEXT NOT NULL,
  dataset TEXT NOT NULL
);

-- 2. Grant access to API's user
-- Note that we do not provide UPDATE.
GRANT SELECT, INSERT ON lev_audit TO :"app_user";
GRANT USAGE ON lev_audit_id_seq TO :"app_user";
