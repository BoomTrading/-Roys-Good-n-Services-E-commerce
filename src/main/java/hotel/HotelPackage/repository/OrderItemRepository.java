package hotel.HotelPackage.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import hotel.HotelPackage.model.Order;
import hotel.HotelPackage.model.OrderItem;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    List<OrderItem> findByOrder(Order order);
}