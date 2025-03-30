package hotel.HotelPackage.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import hotel.HotelPackage.model.Cart;
import hotel.HotelPackage.model.Guest;

import java.util.List;

public interface CartRepository extends JpaRepository<Cart, Integer> {

    // Trova tutti gli elementi del carrello di un ospite
    List<Cart> findByGuest(Guest guest);

    // Elimina tutti gli elementi del carrello di un ospite
    void deleteByGuest(Guest guest);
}