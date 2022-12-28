package uk.gov.gdx.datashare.service

import com.fasterxml.jackson.databind.ObjectMapper
import java.util.HashMap
import kotlin.reflect.full.memberProperties

class EnrichmentService {
  companion object {
    inline fun <reified T : Any> getDataWithOnlyFields(
      mapper: ObjectMapper,
      completeObject: T?,
      enrichmentFields: List<String>
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
      return mapper.convertValue(hashMap, T::class.java)
    }
  }
}