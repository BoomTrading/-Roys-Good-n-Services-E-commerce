package hotel.HotelPackage.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import hotel.HotelPackage.model.AdmUser;
import hotel.HotelPackage.repository.AdmUserRepository;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private AdmUserRepository admUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Try to find by username first, then by email if not found
        AdmUser user = admUserRepository.findByUsername(username)
                .orElseGet(() -> admUserRepository.findByGuestEmail(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found with username/email: " + username)));

        // Determine the user identifier (username for admins, email for guests)
        String userIdentifier = (user.getUsername() != null && !user.getUsername().isEmpty()) ? 
                user.getUsername() : 
                (user.getGuest() != null ? user.getGuest().getEmail() : username);
        
        // Generate authorities from roles
        List<SimpleGrantedAuthority> authorities;
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            // Split roles by comma, normalize each role, and convert to authorities
            authorities = Arrays.stream(user.getRoles().split(","))
                    .map(String::trim)
                    .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
            
            // Make sure ROLE_USER is added for users (if they don't already have it)
            boolean hasUserRole = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"));
            
            if (!hasUserRole) {
                authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            }
            
            // Debug log to check roles
            System.out.println("User '" + userIdentifier + "' roles: " + authorities.stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.joining(", ")));
        } else {
            // Default to ROLE_USER if no roles specified
            authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        }

        // Build and return user details
        return User.builder()
                .username(userIdentifier)
                .password("{noop}" + user.getPassword())
                .authorities(authorities)
                .build();
    }
}
