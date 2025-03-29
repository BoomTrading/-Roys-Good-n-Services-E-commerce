package hotel.booking.repository;

import hotel.booking.model.Cart;
import hotel.booking.model.Guest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartRepository extends JpaRepository<Cart, Integer> {

    // Trova tutti gli elementi del carrello di un ospite
    List<Cart> findByGuest(Guest guest);

    // Elimina tutti gli elementi del carrello di un ospite
    void deleteByGuest(Guest guest);
}