package uk.gov.gdx.datashare

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class GdxDataSharePoc

fun main(args: Array<String>) {
  runApplication<GdxDataSharePoc>(*args)
}
