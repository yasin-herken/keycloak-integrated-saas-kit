package com.archcore.security.converter;

import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Custom JWT Authentication Converter for Keycloak.
 * Extracts realm_access.roles from the JWT and maps them to Spring Security authorities
 * with the ROLE_ prefix.
 */
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String REALM_ACCESS_CLAIM = "realm_access";
    private static final String ROLES_CLAIM = "roles";
    private static final String ROLE_PREFIX = "ROLE_";

    private final JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public Collection<GrantedAuthority> convert(@NonNull Jwt jwt) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        // Extract authorities from standard scopes
        Collection<GrantedAuthority> scopeAuthorities = grantedAuthoritiesConverter.convert(jwt);
        authorities.addAll(scopeAuthorities);

        // Extract realm_access.roles from Keycloak JWT
        authorities.addAll(extractRealmAccessRoles(jwt));

        return authorities;
    }

    private Collection<GrantedAuthority> extractRealmAccessRoles(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        Map<String, Object> realmAccess = jwt.getClaimAsMap(REALM_ACCESS_CLAIM);
        if (realmAccess != null) {
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) realmAccess.get(ROLES_CLAIM);
            if (roles != null) {
                authorities.addAll(
                        roles.stream()
                                .map(role -> new SimpleGrantedAuthority(ROLE_PREFIX + role))
                                .toList()
                );
            }
        }

        return authorities;
    }
}