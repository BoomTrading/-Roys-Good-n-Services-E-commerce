package hotel.booking.service;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import hotel.booking.model.AdmUser;
import hotel.booking.repository.AdmUserRepository;


@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private AdmUserRepository admUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AdmUser user = admUserRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }

        // Usa la password dal database direttamente, senza crittografia
        return User.builder()
            .username(user.getUsername())
            .password("{noop}" + user.getPassword()) // {noop} dice a Spring di non usare crittografia
            .roles(user.getRoles().split(","))
            .build();
    }
}
