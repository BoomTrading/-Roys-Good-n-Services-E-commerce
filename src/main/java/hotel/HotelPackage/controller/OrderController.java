package hotel.HotelPackage.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import hotel.HotelPackage.model.Cart;
import hotel.HotelPackage.model.Guest;
import hotel.HotelPackage.model.Order;
import hotel.HotelPackage.model.OrderItem;
import hotel.HotelPackage.model.Product;
import hotel.HotelPackage.model.Service;
import hotel.HotelPackage.repository.CartRepository;
import hotel.HotelPackage.repository.GuestRepository;
import hotel.HotelPackage.repository.OrderItemRepository;
import hotel.HotelPackage.repository.OrderRepository;
import hotel.HotelPackage.repository.ProductRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private ProductRepository productRepository;

    @GetMapping
    public String listOrders(Model model, Authentication authentication) {
        String email = authentication.getName();
        Guest guest = guestRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Guest not found: " + email));
        List<Order> orders = orderRepository.findByGuest(guest);
        model.addAttribute("orders", orders);
        return "orders";
    }

    @GetMapping("/{id}")
    public String showOrderDetails(@PathVariable("id") int id, Model model, Authentication authentication) {
        String email = authentication.getName();
        Guest guest = guestRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Guest not found: " + email));
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid order Id:" + id));
        if (order.getGuest().getId() != guest.getId()) {
            model.addAttribute("errorMessage", "You are not authorized to view this order.");
            return "error/accessDenied"; // Reindirizza a una pagina di errore personalizzata
        }
        List<OrderItem> items = orderItemRepository.findByOrder(order);
        model.addAttribute("order", order);
        model.addAttribute("items", items);
        return "orderDetails";
    }

    @PostMapping("/create")
    public String createOrder(Authentication authentication, Model model) {
        try {
            String email = authentication.getName();
            Guest guest = guestRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Guest not found: " + email));

            List<Cart> cartItems = cartRepository.findByGuest(guest);
            if (cartItems.isEmpty()) {
                model.addAttribute("errorMessage", "Cart is empty.");
                return "redirect:/cart";
            }

            BigDecimal totalAmount = BigDecimal.ZERO;
            Order order = new Order();
            order.setGuest(guest);
            order.setOrderDate(LocalDateTime.now());
            order.setStatus("PENDING");

            for (Cart cartItem : cartItems) {
                BigDecimal unitPrice;
                if (cartItem.getProduct() != null) {
                    Product product = cartItem.getProduct();
                    if (product.getStock() < cartItem.getQuantity()) {
                        model.addAttribute("errorMessage", "Not enough stock for product: " + product.getName());
                        return "redirect:/cart";
                    }
                    unitPrice = product.getPrice();
                    product.setStock(product.getStock() - cartItem.getQuantity());
                    productRepository.save(product);
                } else {
                    Service service = cartItem.getService();
                    if (!service.isAvailable()) {
                        model.addAttribute("errorMessage", "Service not available: " + service.getName());
                        return "redirect:/cart";
                    }
                    unitPrice = service.getPrice();
                }
                totalAmount = totalAmount.add(unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity())));
            }

            order.setTotalAmount(totalAmount);
            orderRepository.save(order);

            for (Cart cartItem : cartItems) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrder(order);
                orderItem.setProduct(cartItem.getProduct());
                orderItem.setService(cartItem.getService());
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setUnitPrice(cartItem.getProduct() != null ? cartItem.getProduct().getPrice() : cartItem.getService().getPrice());
                orderItemRepository.save(orderItem);
            }

            cartRepository.deleteByGuest(guest);
            model.addAttribute("successMessage", "Order created successfully!");
            return "redirect:/orders";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error creating order: " + e.getMessage());
            return "redirect:/cart";
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/updateStatus/{id}")
    public String updateOrderStatus(@PathVariable("id") int id, @RequestParam("status") String status, Model model) {
        try {
            Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid order Id:" + id));
            order.setStatus(status);
            orderRepository.save(order);
            model.addAttribute("successMessage", "Order status updated successfully!");
            return "redirect:/orders";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error updating order status: " + e.getMessage());
            return "redirect:/orders";
        }
    }
}