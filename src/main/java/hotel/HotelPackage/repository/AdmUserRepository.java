package hotel.HotelPackage.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import hotel.HotelPackage.model.AdmUser;

import java.util.Optional;

public interface AdmUserRepository extends JpaRepository<AdmUser, Integer> {

    // Trova un amministratore per email (usato per l'autenticazione)
    Optional<AdmUser> findByGuestEmail(String email);
    
    // Trova un amministratore per username (usato per l'autenticazione)
    Optional<AdmUser> findByUsername(String username);
}