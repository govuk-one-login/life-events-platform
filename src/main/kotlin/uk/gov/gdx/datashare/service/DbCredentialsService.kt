package uk.gov.gdx.datashare.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.rds.RdsUtilities
import software.amazon.awssdk.services.rds.model.GenerateAuthenticationTokenRequest

@Service
final class DbCredentialsService(
  @Value("\${db.provider}") val dbProvider: DbProvider,
  @Value("\${spring.datasource.username}") val dbUsername: String,
  @Value("\${db.port:#{null}}") val dbPort: Int?,
  @Value("\${db.hostname:#{null}}") val dbHostname: String?,
  @Value("\${spring.datasource.password:#{null}}") val dbPassword: String?,
  @Value("\${db.rds-region:#{null}}") val dbRdsRegion: String?,
) {
  enum class DbProvider {
    LOCAL,
    RDS,
  }

  private val rdsUtilities: RdsUtilities? = if (dbProvider == DbProvider.RDS) {
    RdsUtilities.builder()
      .credentialsProvider(DefaultCredentialsProvider.create())
      .region(Region.of(dbRdsRegion))
      .build()
  } else { null }

  fun username() = dbUsername

  fun password(): String {
    return when (dbProvider) {
      DbProvider.LOCAL ->
        dbPassword
          ?: throw IllegalStateException("No database password provided for local configuration")

      DbProvider.RDS -> getToken()
    }
  }

  private fun getToken(): String {
    return rdsUtilities!!.generateAuthenticationToken(
      GenerateAuthenticationTokenRequest.builder()
        .hostname(dbHostname ?: throw IllegalStateException("No database hostname provided"))
        .port(dbPort ?: throw IllegalStateException("No database port provided"))
        .username(dbUsername)
        .build(),
    )
  }
}
