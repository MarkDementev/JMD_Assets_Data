package fund.data.assets.config;

import fund.data.assets.jwt.JWTAuthenticationFilter;
import fund.data.assets.jwt.JWTAuthorizationFilter;
import fund.data.assets.jwt.JWTHelper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * @version 0.6-a
 * @author MarkDementev a.k.a JavaMarkDem
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    public static final String ADMIN_NAME = "admin";
    public static final String ADMIN_PASSWORD = "password";
    public static final String LOGIN_PATH = "/login";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_USER = "ROLE_USER";
    private final String baseUrl;
    private final RequestMatcher loginRequest;
    private final JWTHelper jwtHelper;
    private final RequestMatcher publicUrl;

    public SecurityConfig(@Value("${base-url}") final String baseUrl, final JWTHelper jwtHelper) {
        this.baseUrl = baseUrl;
        this.loginRequest = new AntPathRequestMatcher(LOGIN_PATH, POST.toString());
        this.jwtHelper = jwtHelper;
        this.publicUrl = new OrRequestMatcher(loginRequest);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authenticationProvider(authenticationProvider())
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers(loginRequest).permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilter(
                        new JWTAuthenticationFilter(
                                authenticationManager(http.getSharedObject(AuthenticationConfiguration.class)),
                                loginRequest,
                                jwtHelper)
                )
                .addFilterBefore(
                        new JWTAuthorizationFilter(publicUrl, jwtHelper),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();

        authenticationProvider.setUserDetailsService(adminService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public UserDetailsService adminService() {
        UserDetails admin = User.builder()
                .username(ADMIN_NAME)
                .password(passwordEncoder().encode(ADMIN_PASSWORD))
                .authorities(ROLE_ADMIN)
                .build();

        return new InMemoryUserDetailsManager(admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
