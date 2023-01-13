package uk.gov.gdx.datashare.service

import com.fasterxml.jackson.databind.ObjectMapper
import kotlin.reflect.full.memberProperties

class EnrichmentService {
  companion object {
    inline fun <reified T : Any> getDataWithOnlyFields(
      objectMapper: ObjectMapper,
      completeObject: T?,
      enrichmentFields: List<String>,
    ): T? {
      if (completeObject == null) {
        return null
      }
      val hashMap = HashMap<String, Any?>()
      for (prop in T::class.memberProperties) {
        if (enrichmentFields.contains(prop.name)) {
          val value = prop.getter.call(completeObject)
          hashMap[prop.name] = value
        }
      }
      return objectMapper.convertValue(hashMap, T::class.java)
    }
  }
}
