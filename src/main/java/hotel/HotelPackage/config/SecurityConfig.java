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

    // Iniettiamo il servizio che gestisce il recupero dei dettagli utente per
    // l'autenticazione
    @Autowired
    private UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Disabilitiamo CSRF per semplicità (da valutare in base alla sicurezza
                // dell'app)
                .csrf(csrf -> csrf.disable())

                // Impostiamo il servizio personalizzato per il recupero dell'utente autenticato
                .userDetailsService(userDetailsService)

                // Definizione delle autorizzazioni per gli endpoint
                .authorizeHttpRequests(auth -> auth
                        // Risorse pubbliche accessibili da chiunque
                        .requestMatchers("/login", "/css/**", "/js/**", "/images/**").permitAll()

                        // Percorsi accessibili solo da utenti con ruolo ADMIN
                        .requestMatchers("/admin/**", "/cart/all", "/cart/guest/**", "/orders/all").hasRole("ADMIN")

                        // Percorsi accessibili sia da utenti con ruolo USER che ADMIN
                        .requestMatchers(
                                "/cart/**",
                                "/orders",
                                "/orders/checkout",
                                "/orders/create",
                                "/orders/{id}",
                                "/payments/**")
                        .hasAnyRole("USER", "ADMIN")

                        // Tutti gli altri percorsi richiedono autenticazione
                        .anyRequest().authenticated())

                // Configurazione del login form personalizzato
                .formLogin(form -> form
                        .loginPage("/login") // Pagina di login personalizzata
                        .defaultSuccessUrl("/", true) // Reindirizzamento dopo login riuscito
                        .permitAll()) // Accesso consentito a tutti

                // Configurazione del logout
                .logout(logout -> logout
                        .logoutUrl("/logout") // Endpoint per effettuare logout
                        .logoutSuccessUrl("/login?logout") // Pagina di redirect post logout
                        .permitAll());

        // Costruzione e restituzione del filtro di sicurezza configurato
        return http.build();
    }
}
