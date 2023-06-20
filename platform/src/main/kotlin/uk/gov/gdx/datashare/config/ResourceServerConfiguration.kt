package uk.gov.gdx.datashare.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class ResourceServerConfiguration {
  @Autowired
  fun configureGlobal(
    @Value("\${prometheus.user.name}") userName: String,
    @Value("\${prometheus.user.password}") password: String,
    @Value("\${prometheus.user.role}") role: String,
    authenticationManagerBuilder: AuthenticationManagerBuilder,
    passwordEncoder: PasswordEncoder,
  ) {
    authenticationManagerBuilder
      .inMemoryAuthentication()
      .withUser(userName)
      .password(passwordEncoder.encode(password))
      .roles(role)
  }

  @Order(1)
  @Bean
  fun permittedFilterChain(http: HttpSecurity): SecurityFilterChain {
    return http
      .csrf { it.disable() } // csrf not needed on a rest api
      .securityMatcher(
        "/favicon.ico",
        "/info",
        "/v3/api-docs/**",
        "/v3/api-docs.yaml",
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/metrics/**",
      )
      .authorizeHttpRequests {
        it.anyRequest().permitAll()
      }
      .build()
  }

  @Order(2)
  @Bean
  fun prometheusFilterChain(http: HttpSecurity): SecurityFilterChain {
    return http
      .csrf { it.disable() }
      .securityMatcher("/prometheus")
      .authorizeHttpRequests {
        it.anyRequest().hasRole("ACTUATOR")
      }
      .httpBasic {}
      .build()
  }

  @Order(3)
  @Bean
  fun healthFilterChain(http: HttpSecurity): SecurityFilterChain {
    return http
      .csrf { it.disable() }
      .securityMatcher("/health/**")
      .authorizeHttpRequests {
        it.anyRequest().permitAll()
      }
      .httpBasic {}
      .build()
  }

  @Order(Ordered.LOWEST_PRECEDENCE)
  @Bean
  fun apiFilterChain(http: HttpSecurity): SecurityFilterChain {
    return http
      .csrf { it.disable() }
      .authorizeHttpRequests {
        it.anyRequest().authenticated()
      }
      .oauth2ResourceServer { it.jwt {} }
      .build()
  }
}
