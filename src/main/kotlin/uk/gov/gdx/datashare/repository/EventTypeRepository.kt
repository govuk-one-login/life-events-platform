package uk.gov.gdx.datashare.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface EventTypeRepository : CrudRepository<EventType, String>
