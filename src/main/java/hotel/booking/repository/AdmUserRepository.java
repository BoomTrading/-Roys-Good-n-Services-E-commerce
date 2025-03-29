package hotel.booking.repository;

import hotel.booking.model.AdmUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdmUserRepository extends JpaRepository<AdmUser, Integer> {

    // Trova un amministratore per username (usato per l'autenticazione)
    Optional<AdmUser> findByUsername(String username);
}