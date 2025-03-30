package hotel.HotelPackage.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import hotel.HotelPackage.model.Guest;
import hotel.HotelPackage.model.Order;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    // Trova tutti gli ordini di un ospite
    List<Order> findByGuest(Guest guest);
}