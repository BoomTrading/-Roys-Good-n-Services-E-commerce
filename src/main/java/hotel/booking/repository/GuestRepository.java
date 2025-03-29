package hotel.booking.repository;

import hotel.booking.model.Guest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GuestRepository extends JpaRepository<Guest, Integer> {

    // Cerca ospiti in base a un pattern (usato per la ricerca e l'autocompletamento)
    @Query("SELECT g FROM Guest g WHERE g.firstName LIKE %:pattern% OR g.lastName LIKE %:pattern% OR g.email LIKE %:pattern%")
    List<Guest> findByPatternLike(@Param("pattern") String pattern);

    // Cerca un ospite per email (usato per l'autenticazione nel carrello e negli ordini)
    Optional<Guest> findByEmail(String email);
}