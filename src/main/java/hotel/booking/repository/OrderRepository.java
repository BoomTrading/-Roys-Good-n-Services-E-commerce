package hotel.booking.repository;

import hotel.booking.model.Guest;
import hotel.booking.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    // Trova tutti gli ordini di un ospite
    List<Order> findByGuest(Guest guest);
}