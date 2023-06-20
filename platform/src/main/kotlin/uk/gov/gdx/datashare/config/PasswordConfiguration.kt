package uk.gov.gdx.datashare.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class PasswordConfiguration {
  @Bean
  fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
