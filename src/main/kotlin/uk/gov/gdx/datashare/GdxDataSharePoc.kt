package uk.gov.gdx.datashare

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "10m")
@ConfigurationPropertiesScan
class GdxDataSharePoc

fun main(args: Array<String>) {
  runApplication<GdxDataSharePoc>(*args)
}
