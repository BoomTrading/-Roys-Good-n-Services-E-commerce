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
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private AdmUserRepository admUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // First try to find by username (for admin users)
        AdmUser user = admUserRepository.findByUsername(username)
                .orElse(null);
        
        // If not found by username, try to find by guest email
        if (user == null) {
            user = admUserRepository.findByGuestEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with username/email: " + username));
        }

        // Convert comma-separated roles to list of authorities
        List<SimpleGrantedAuthority> authorities = Arrays.stream(user.getRoles().split(","))
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.trim()))
                .collect(Collectors.toList());

        // Build UserDetails with explicit authorities instead of roles
        return User.builder()
                .username(user.getUsername() != null ? user.getUsername() : user.getGuest().getEmail())
                .password("{noop}" + user.getPassword())
                .authorities(authorities)
                .build();
    }
}
