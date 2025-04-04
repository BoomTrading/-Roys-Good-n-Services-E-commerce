package hotel.HotelPackage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hotel.HotelPackage.model.Product;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    // Cerca prodotti in base a un pattern
    @Query("SELECT p FROM Product p WHERE p.name LIKE %:pattern% OR p.description LIKE %:pattern% OR p.category LIKE %:pattern%")
    List<Product> findByPatternLike(@Param("pattern") String pattern);
    
    // Add method to find products by category
    List<Product> findByCategory(String category);
    
    // Add method to get all distinct categories
    @Query("SELECT DISTINCT p.category FROM Product p ORDER BY p.category")
    List<String> findDistinctCategories();
}