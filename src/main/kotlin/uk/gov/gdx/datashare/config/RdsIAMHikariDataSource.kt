package uk.gov.gdx.datashare.config

import com.zaxxer.hikari.HikariDataSource
import uk.gov.gdx.datashare.service.DbCredentialsService

class RdsIAMHikariDataSource(private val dbCredentialsService: DbCredentialsService) : HikariDataSource() {
  override fun getPassword() = dbCredentialsService.password()
}
