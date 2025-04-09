package hotel.HotelPackage.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import hotel.HotelPackage.model.Order;
import hotel.HotelPackage.model.Guest;
import hotel.HotelPackage.repository.GuestRepository;
import hotel.HotelPackage.repository.OrderRepository;

import java.util.Optional;

@Component
public class OrderSecurity {

    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private GuestRepository guestRepository;

    public boolean isOrderOwner(Authentication authentication, int orderId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        // Get current username
        String username = authentication.getName();
        
        // If admin, always return true
        if (username.startsWith("admin_")) {
            return true;
        }
        
        // Find the guest associated with the username
        Optional<Guest> guestOpt = guestRepository.findByEmail(username);
        if (guestOpt.isEmpty()) {
            return false;
        }
        
        // Find the order
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return false;
        }
        
        // Check if the guest is the owner of the order
        return order.getGuest().getId() == guestOpt.get().getId();
    }
}
