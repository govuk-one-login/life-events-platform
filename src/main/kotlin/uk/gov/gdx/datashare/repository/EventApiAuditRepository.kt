package uk.gov.gdx.datashare.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface EventApiAuditRepository : CrudRepository<EventApiAudit, UUID>
