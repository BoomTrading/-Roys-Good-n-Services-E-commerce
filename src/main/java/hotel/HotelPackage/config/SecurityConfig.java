package hotel.HotelPackage.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.userdetails.UserDetailsService;

@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
@Configuration
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .userDetailsService(userDetailsService)
            .authorizeHttpRequests(authorize -> authorize
                // Public resources
                .requestMatchers("/login", "/css/**", "/js/**", "/images/**").permitAll()
                // Admin-only paths
                .requestMatchers("/admin/**", "/cart/all", "/cart/guest/**").hasRole("ADMIN")
                // User-required paths
                .requestMatchers("/cart/**", "/orders/**").hasAnyRole("USER", "ADMIN")
                // Payments page accessible to users and admins
                .requestMatchers("/payments/**").hasAnyRole("USER", "ADMIN")
                // Allow authenticated users to access other paths
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll());

        return http.build();
    }
}
