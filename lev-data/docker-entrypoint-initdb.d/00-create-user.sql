\set app_user `echo "${APP_USER}"`
\set app_password `echo "${APP_PASSWORD}"`

CREATE USER :"app_user" WITH PASSWORD :'app_password';
