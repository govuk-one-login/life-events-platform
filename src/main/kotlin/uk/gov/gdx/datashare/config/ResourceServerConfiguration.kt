package uk.gov.gdx.datashare.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class ResourceServerConfiguration {

  @Bean
  fun filterChain(http: HttpSecurity): SecurityFilterChain {
    return http
      .csrf { it.disable() } // csrf not needed on a rest api
      .authorizeHttpRequests { requests ->
        requests.antMatchers(
          "/webjars/**", "/favicon.ico", "/csrf",
          "/health/**", "/info", "/h2-console/**",
          "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
          "/queue-admin/retry-all-dlqs", "/metrics/**",
        ).permitAll().anyRequest().authenticated()
      }
      .oauth2ResourceServer { it.jwt() }
      .build()
  }
}
