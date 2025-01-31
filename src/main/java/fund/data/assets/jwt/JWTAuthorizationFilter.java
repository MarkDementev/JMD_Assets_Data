package fund.data.assets.jwt;

import java.io.IOException;

import java.util.List;
import java.util.Optional;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import static fund.data.assets.config.SecurityConfig.ADMIN_NAME;
import static fund.data.assets.config.SecurityConfig.ROLE_ADMIN;
import static fund.data.assets.config.SecurityConfig.ROLE_USER;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY;

/**
 * @version 0.6-a
 * @author MarkDementev a.k.a JavaMarkDem
 */
public class JWTAuthorizationFilter extends OncePerRequestFilter {
    private static final String BEARER = "Bearer";
    private final RequestMatcher publicUrl;
    private final JWTHelper jwtHelper;

    public JWTAuthorizationFilter(final RequestMatcher publicUrl, final JWTHelper jwtHelper) {
        this.publicUrl = publicUrl;
        this.jwtHelper = jwtHelper;
    }

    @Override
    protected boolean shouldNotFilter(final HttpServletRequest request) {
        return publicUrl.matches(request);
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain filterChain) throws ServletException, IOException {
        final var authToken = Optional.ofNullable(request.getHeader(AUTHORIZATION))
                .map(header -> header.replaceFirst("^" + BEARER, ""))
                .map(String::trim)
                .map(jwtHelper::verify)
                .map(claims -> claims.get(SPRING_SECURITY_FORM_USERNAME_KEY))
                .map(Object::toString)
                .map(this::buildAuthToken)
                .orElse(null);

        SecurityContextHolder.getContext().setAuthentication(authToken);
        filterChain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken buildAuthToken(final String username) {
        String role = username.equals(ADMIN_NAME) ? ROLE_ADMIN : ROLE_USER;

        return new UsernamePasswordAuthenticationToken(
                username,
                null,
                List.of(new SimpleGrantedAuthority(role))
        );
    }
}
