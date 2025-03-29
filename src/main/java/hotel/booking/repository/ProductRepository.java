package hotel.booking.repository;

import hotel.booking.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    // Cerca prodotti in base a un pattern
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:pattern% OR p.description LIKE %:pattern% OR p.category LIKE %:pattern%")
    List<Product> findByPatternLike(@Param("pattern") String pattern);
}