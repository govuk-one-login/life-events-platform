package uk.gov.gdx.datashare.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface DataConsumerRepository : CoroutineCrudRepository<DataConsumer, String> {

  @Query("SELECT dc.* FROM data_consumer dc where dc.legacy_ftp = :legacy")
  fun findAllByLegacyFtp(legacy: Boolean): Flow<DataConsumer>
}
