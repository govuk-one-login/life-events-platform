package uk.gov.gdx.datashare.config

import org.apache.commons.lang3.RegExUtils
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import java.util.*
import java.util.stream.Collectors

@Component
class AuthenticationFacade {

  fun getAuthentication(): Authentication =
    SecurityContextHolder.getContext().authentication

  fun getUsername(): String {
    val userPrincipal = getAuthentication().principal as Jwt
    return userPrincipal.getClaim("sub")
  }

  fun hasRoles(vararg allowedRoles: String): Boolean {
    val roles = Arrays.stream(allowedRoles)
      .map { r: String? -> RegExUtils.replaceFirst(r, "ROLE_", "") }
      .collect(Collectors.toList())
    return hasMatchingRole(roles, getAuthentication())
  }

  private fun hasMatchingRole(roles: List<String>, authentication: Authentication?): Boolean {
    return authentication != null &&
      authentication.authorities.stream()
        .anyMatch { a: GrantedAuthority? -> roles.contains(RegExUtils.replaceFirst(a!!.authority, "ROLE_", "")) }
  }
}
