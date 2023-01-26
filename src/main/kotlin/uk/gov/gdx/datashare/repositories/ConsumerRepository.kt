package uk.gov.gdx.datashare.repositories

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ConsumerRepository : CrudRepository<Consumer, UUID>
