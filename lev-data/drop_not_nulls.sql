/* These columns had not null constraints, but the corresponding columns in the
   data json had null values. Removed constraints to keep new data consistent.
 */

ALTER TABLE death_registration_v1
    alter forenames drop not null,
    alter surname drop not null,
    alter date_of_death drop not null
