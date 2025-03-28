package hotel.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import hotel.booking.model.AdmUser;

public interface AdmUserRepository extends JpaRepository<AdmUser, Integer> {
   
    AdmUser findByUsername(String username);
}