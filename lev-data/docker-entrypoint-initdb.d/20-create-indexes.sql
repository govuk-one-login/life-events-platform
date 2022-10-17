\set app_user `echo "${APP_USER}"`

CREATE EXTENSION IF NOT EXISTS unaccent;
CREATE OR REPLACE FUNCTION anglicise(x TEXT) RETURNS TEXT
AS $$
  SELECT lower(unaccent(x))
$$ LANGUAGE SQL
IMMUTABLE
RETURNS NULL ON NULL INPUT;

GRANT EXECUTE ON FUNCTION anglicise(TEXT) TO :"app_user";

CREATE INDEX IF NOT EXISTS birth_registration_v1_search_idx ON birth_registration_v1 (
  date_of_birth,
  anglicise(surname),
  anglicise(forenames)
);

CREATE INDEX IF NOT EXISTS death_registration_v1_search_idx ON death_registration_v1 (
  date_of_birth,
  anglicise(surname),
  anglicise(forenames)
);
CREATE INDEX IF NOT EXISTS death_registration_v1_search_dod_idx ON death_registration_v1 (
  date_of_death,
  anglicise(surname),
  anglicise(forenames)
);

CREATE INDEX IF NOT EXISTS marriage_registration_v1_search_idx ON marriage_registration_v1 (
  bride_date_of_birth,
  anglicise(bride_surname),
  anglicise(bride_forenames),
  groom_date_of_birth,
  anglicise(groom_surname),
  anglicise(groom_forenames)
);
CREATE INDEX IF NOT EXISTS marriage_registration_v1_search_groom_idx ON marriage_registration_v1 (
  groom_date_of_birth,
  anglicise(groom_surname),
  anglicise(groom_forenames),
  bride_date_of_birth,
  anglicise(bride_surname),
  anglicise(bride_forenames)
);
