package hotel.booking.repository;

import hotel.booking.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ServiceRepository extends JpaRepository<Service, Integer> {

    // Cerca servizi in base a un pattern
    @Query("SELECT s FROM Service s WHERE s.name LIKE %:pattern% OR s.description LIKE %:pattern% OR s.category LIKE %:pattern%")
    List<Service> findByPatternLike(@Param("pattern") String pattern);
}