package ph.edu.catc.labtrack.security;

import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain security(HttpSecurity http) throws Exception {
        var login = new LoginUrlAuthenticationEntryPoint("/login");
        var basic = new BasicAuthenticationEntryPoint();
        basic.setRealmName("CATC LabTrack");
        basic.afterPropertiesSet();
        return http.authorizeHttpRequests(auth -> auth
                .dispatcherTypeMatchers(jakarta.servlet.DispatcherType.ERROR).permitAll()
                .requestMatchers("/login", "/css/login.css").permitAll()
                .requestMatchers("/admin", "/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/equipment/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/equipment/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/equipment/**").hasRole("ADMIN")
                .anyRequest().hasAnyRole("USER", "ADMIN"))
            .formLogin(form -> form.loginPage("/login").defaultSuccessUrl("/", true).permitAll())
            .logout(logout -> logout.logoutSuccessUrl("/login?logout"))
            .httpBasic(api -> api.authenticationEntryPoint(basic))
            .exceptionHandling(errors -> errors.authenticationEntryPoint((request, response, ex) -> {
                if (request.getServletPath().startsWith("/api/")) {
                    basic.commence(request, response, ex);
                } else {
                    login.commence(request, response, ex);
                }
            }))
            .build();
    }

    @Bean
    BCryptPasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    UserDetailsService accounts(JdbcTemplate jdbc) {
        return name -> jdbc.query("select username, password_hash, role from app_user where username = ?",
            (rs, row) -> User.withUsername(rs.getString("username")).password(rs.getString("password_hash"))
                .roles(rs.getString("role")).build(), name)
            .stream().findFirst().orElseThrow(() -> new UsernameNotFoundException("Unknown account"));
    }
}
