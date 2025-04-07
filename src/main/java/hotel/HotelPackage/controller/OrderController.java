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
import hotel.HotelPackage.model.Payment;
import hotel.HotelPackage.model.Product;
import hotel.HotelPackage.model.Service;
import hotel.HotelPackage.repository.CartRepository;
import hotel.HotelPackage.repository.GuestRepository;
import hotel.HotelPackage.repository.OrderItemRepository;
import hotel.HotelPackage.repository.OrderRepository;
import hotel.HotelPackage.repository.PaymentRepository;
import hotel.HotelPackage.repository.ProductRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
    
    @Autowired
    private PaymentRepository paymentRepository;

    @GetMapping
    public String listOrders(Model model, Authentication authentication) {
        String username = authentication.getName();
        
        // Check if an admin is viewing the page
        if (username.startsWith("admin_")) {
            // For admin users, redirect to admin order view
            return "redirect:/orders/all";
        }
        
        Guest guest = guestRepository.findByEmail(username)
            .orElseThrow(() -> new IllegalArgumentException("Guest not found: " + username));
        List<Order> orders = orderRepository.findByGuest(guest);
        model.addAttribute("orders", orders);
        return "orders";
    }

    @GetMapping("/{id}")
    public String showOrderDetails(@PathVariable("id") int id, Model model, Authentication authentication) {
        String username = authentication.getName();
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid order Id:" + id));
            
        // For admin users, allow viewing any order
        if (username.startsWith("admin_")) {
            List<OrderItem> items = orderItemRepository.findByOrder(order);
            List<Payment> payments = paymentRepository.findByOrder(order);
            model.addAttribute("order", order);
            model.addAttribute("items", items);
            model.addAttribute("payments", payments);
            model.addAttribute("isAdmin", true);
            return "orderDetails";
        }
        
        // For regular users, check if they own the order
        Guest guest = guestRepository.findByEmail(username)
            .orElseThrow(() -> new IllegalArgumentException("Guest not found: " + username));
        if (order.getGuest().getId() != guest.getId()) {
            model.addAttribute("errorMessage", "You are not authorized to view this order.");
            return "error/accessDenied";
        }
        
        List<OrderItem> items = orderItemRepository.findByOrder(order);
        model.addAttribute("order", order);
        model.addAttribute("items", items);
        return "orderDetails";
    }
    
    @GetMapping("/checkout")
    public String showCheckout(Model model, Authentication authentication) {
        String username = authentication.getName();
        
        // Admin users don't have access to the checkout page
        if (username.startsWith("admin_")) {
            return "redirect:/cart";
        }
        
        Optional<Guest> guestOpt = guestRepository.findByEmail(username);
        if (guestOpt.isEmpty()) {
            model.addAttribute("errorMessage", "User account not linked to a guest profile");
            return "redirect:/cart";
        }
        
        Guest guest = guestOpt.get();
        List<Cart> cartItems = cartRepository.findByGuest(guest);
        
        if (cartItems.isEmpty()) {
            model.addAttribute("errorMessage", "Your cart is empty. Add items before checkout.");
            return "redirect:/cart";
        }
        
        // Calculate total
        BigDecimal total = BigDecimal.ZERO;
        for (Cart item : cartItems) {
            BigDecimal price;
            if (item.getProduct() != null) {
                price = item.getProduct().getPrice();
            } else {
                price = item.getService().getPrice();
            }
            total = total.add(price.multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", total);
        model.addAttribute("guest", guest);
        
        return "checkout";
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
            return "redirect:/orders/" + order.getId();
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
            return "redirect:/orders/" + id;
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error updating order status: " + e.getMessage());
            return "redirect:/orders";
        }
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public String listAllOrders(Model model) {
        List<Order> orders = orderRepository.findAll();
        model.addAttribute("orders", orders);
        model.addAttribute("isAdmin", true);
        return "allOrders";
    }
    
    @GetMapping("/guest/{guestId}")
    @PreAuthorize("hasRole('ADMIN')")
    public String listOrdersByGuest(@PathVariable("guestId") int guestId, Model model) {
        Guest guest = guestRepository.findById(guestId)
            .orElseThrow(() -> new IllegalArgumentException("Invalid guest Id:" + guestId));
        List<Order> orders = orderRepository.findByGuest(guest);
        model.addAttribute("orders", orders);
        model.addAttribute("guestName", guest.getFirstName() + " " + guest.getLastName());
        model.addAttribute("guestId", guest.getId());
        model.addAttribute("isAdmin", true);
        return "guestOrders";
    }
}