package uk.gov.gdx.datashare.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
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

  @Bean
  fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
    return http
      .csrf { it.disable() } // csrf not needed on a rest api
      .authorizeHttpRequests { requests ->
        requests
          .requestMatchers("/prometheus").hasRole("ACTUATOR").and().httpBasic()
        requests
          .requestMatchers(
            "/favicon.ico",
            "/health/**",
            "/info",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/metrics/**",
          ).permitAll()
          .anyRequest().authenticated()
      }
      .oauth2ResourceServer { it.jwt() }
      .build()
  }
}
