package uk.gov.di.data.lep.library.dto.deathnotification;

public record DeathNotificationSet(
    String aud,
    DeathRegistrationEventMapping events,
    Long exp,
    Long iat,
    String iss,
    String jti,
    Long nbf,
    String sub,
    Long toe,
    String txn
) {
}
