package uk.gov.gdx.datashare.service

import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

class EnrichmentService {
  companion object {
    inline fun <reified T : Any> enrichFields(completeObject: T?, consumerObject: T, enrichmentFields: List<String>): T? {
      if (completeObject == null) {
        return null
      }
      for (prop in T::class.memberProperties) {
        if (enrichmentFields.contains(prop.name) && prop is KMutableProperty<*>) {
          val value = prop.getter.call(completeObject)
          prop.setter.call(consumerObject, value)
        }
      }
      return consumerObject
    }
  }
}