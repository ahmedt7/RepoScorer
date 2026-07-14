package org.redCare.api.service.repoScorer.configuration;

import static java.util.stream.Collectors.toSet;
import static org.springframework.util.StringUtils.hasText;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public class SecurityHeadersFilter extends OncePerRequestFilter {

  public static final String API_CLIENT_ROLES_HEADER = "X-ApiClient-Roles";

  @Override
  protected void doFilterInternal(
          @NotNull HttpServletRequest request,
          @NonNull  HttpServletResponse response,
          @NonNull  FilterChain filterChain)
      throws ServletException, IOException {
    if ("/health".equals(request.getRequestURI())) {
      filterChain.doFilter(request, response);
      return;
    }
    SecurityContext securityContext = SecurityContextHolder.getContext();
    Set<GrantedAuthority> authorities = collectAuthorities(request);
    try {
      Authentication authentication = new UsernamePasswordAuthenticationToken(
              request.getRemoteUser(),
              null,
              authorities
      );
      securityContext.setAuthentication(authentication);
      log.debug("Authentication in SecurityContext is set to {}", authentication);
    } catch (Exception e) {
      String id = UUID.randomUUID().toString();
      log.error("Authentication error {}", id, e);
      response.sendError(
          HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Authentication error %s".formatted(id));
      return;
    }
    filterChain.doFilter(request, response);
  }

  private Set<GrantedAuthority> collectAuthorities(HttpServletRequest request) {
    return new HashSet<>(apiRolesHeaderAuthorities(request));
  }

  private Collection<? extends GrantedAuthority> apiRolesHeaderAuthorities(
      HttpServletRequest request) {
    return Optional.of(request).map(this::rolesFromRequest).orElseGet(Collections::emptySet);
  }

  private Collection<? extends GrantedAuthority> rolesFromRequest(HttpServletRequest request) {
    String roles = request.getHeader(API_CLIENT_ROLES_HEADER);
    if (!hasText(roles)) {
      return Collections.emptySet();
    }
    return Stream.of(roles.split("[, ]"))
        .filter(StringUtils::hasText)
        .map(role -> "ROLE_" + role)
        .map(SimpleGrantedAuthority::new)
        .collect(toSet());
  }

}
