package hotel.HotelPackage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hotel.HotelPackage.model.Service;

import java.util.List;

public interface ServiceRepository extends JpaRepository<Service, Integer> {

    // Search services by pattern (case-insensitive)
    @Query("SELECT s FROM Service s WHERE UPPER(s.name) LIKE UPPER(CONCAT('%', :pattern, '%')) " +
           "OR UPPER(s.description) LIKE UPPER(CONCAT('%', :pattern, '%')) " +
           "OR UPPER(s.category) LIKE UPPER(CONCAT('%', :pattern, '%'))")
    List<Service> findByPatternLike(@Param("pattern") String pattern);

    // Find services by category (case-insensitive)
    @Query("SELECT s FROM Service s WHERE UPPER(s.category) = UPPER(:category)")
    List<Service> findByCategory(@Param("category") String category);

    // Get all distinct categories
    @Query("SELECT DISTINCT s.category FROM Service s ORDER BY s.category")
    List<String> findDistinctCategories();
}