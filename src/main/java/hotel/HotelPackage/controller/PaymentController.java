package hotel.HotelPackage.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import hotel.HotelPackage.model.*;
import hotel.HotelPackage.repository.*;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/payments")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')") // Allow both USER and ADMIN roles
public class PaymentController {

    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ServiceRepository serviceRepository;
    
    @Autowired
    private RoomRepository roomRepository;
    
    @Autowired 
    private BookingRepository bookingRepository;
    
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;
    
    @Autowired
    private AdmUserRepository admUserRepository;

    // **Read: Lista di tutti i pagamenti**
    @GetMapping
    public String showPayments(Model model, Authentication authentication) {
        String username = authentication.getName();
        List<Payment> payments;
        
        if (username.startsWith("admin_")) {
            payments = paymentRepository.findAll(); // Admin sees all payments
            model.addAttribute("isAdmin", true);
        } else {
            // First try to find associated AdmUser
            Optional<AdmUser> userOpt = admUserRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                // If not found by username, try by guest email
                userOpt = admUserRepository.findByGuestEmail(username);
            }

            if (userOpt.isEmpty() || userOpt.get().getGuest() == null) {
                model.addAttribute("errorMessage", "User account not linked to a guest profile. Please contact support.");
                model.addAttribute("payments", new ArrayList<Payment>());
                model.addAttribute("isAdmin", false);
                return "payments";
            }

            Guest guest = userOpt.get().getGuest();
            List<Payment> bookingPayments = paymentRepository.findByBookingGuest(guest);
            List<Payment> orderPayments = paymentRepository.findByOrder_Guest(guest);

            // Combine both lists
            payments = new ArrayList<>(bookingPayments);
            payments.addAll(orderPayments);
            model.addAttribute("isAdmin", false);
        }
        
        model.addAttribute("payments", payments);
        return "payments";
    }

    // **Read: Dettagli di un pagamento**
    @GetMapping("/{id}")
    public String showPaymentDetails(@PathVariable("id") int id, Model model) {
        Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid payment Id:" + id));
        model.addAttribute("payment", payment);
        return "paymentDetails"; // Vista Thymeleaf per i dettagli
    }
    
    // **Read: Dettagli di un pagamento (endpoint alternativo)**
    @GetMapping("/details/{id}")
    public String showPaymentDetailsAlternative(@PathVariable("id") int id, Model model) {
        Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid payment Id:" + id));
        model.addAttribute("payment", payment);
        return "paymentDetails"; // Vista Thymeleaf per i dettagli
    }

    // **Create: Mostra form per nuovo pagamento**
    @GetMapping("/new")
    public String showNewPaymentForm(Model model) {
        model.addAttribute("payment", new Payment());
        // Add products, services, rooms, bookings and orders to the model
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("services", serviceRepository.findAll());
        model.addAttribute("rooms", roomRepository.findAll());
        model.addAttribute("bookings", bookingRepository.findAll());
        model.addAttribute("orders", orderRepository.findAll());
        return "newPayment"; // Vista Thymeleaf per il form
    }

    // **Create: Salva nuovo pagamento with auto-calculation**
    @PostMapping("/ins")
    public String savePayment(
            @ModelAttribute("payment") Payment payment,
            @RequestParam(value = "productId", required = false) Integer productId,
            @RequestParam(value = "serviceId", required = false) Integer serviceId,
            @RequestParam(value = "roomId", required = false) Integer roomId,
            @RequestParam(value = "bookingId", required = false) Integer bookingId,
            @RequestParam(value = "orderId", required = false) Integer orderId,
            @RequestParam(value = "quantity", required = false, defaultValue = "1") Integer quantity,
            Model model) {
        try {
            // Set default values if necessary
            if (payment.getPaymentDate() == null) {
                payment.setPaymentDate(LocalDateTime.now());
            }
            
            // Default quantity to 1 if not provided
            if (quantity == null || quantity < 1) {
                quantity = 1;
            }
            payment.setQuantity(quantity);
            
            // Explicitly set all reference fields to null initially
            payment.setProduct(null);
            payment.setService(null);
            payment.setRoom(null);
            payment.setBooking(null);
            payment.setOrder(null);
            
            // Calculate the amount based on the selected entity
            BigDecimal amount = null;
            boolean hasValidReference = false;
            
            if (productId != null) {
                Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid product Id: " + productId));
                payment.setProduct(product);
                amount = product.getPrice().multiply(BigDecimal.valueOf(quantity));
                hasValidReference = true;
            } else if (serviceId != null) {
                Service service = serviceRepository.findById(serviceId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid service Id: " + serviceId));
                payment.setService(service);
                amount = service.getPrice().multiply(BigDecimal.valueOf(quantity));
                hasValidReference = true;
            } else if (roomId != null) {
                Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid room Id: " + roomId));
                payment.setRoom(room);
                amount = room.getPrice().multiply(BigDecimal.valueOf(quantity));
                hasValidReference = true;
            } else if (bookingId != null) {
                Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid booking Id: " + bookingId));
                payment.setBooking(booking);
                
                // Calculate the total price based on the booking room price and duration
                long days = java.time.temporal.ChronoUnit.DAYS.between(
                        booking.getCheckIn(), 
                        booking.getCheckOut()
                );
                days = Math.max(1, days); // Ensure minimum 1 day
                amount = booking.getRoom().getPrice().multiply(BigDecimal.valueOf(days));
                hasValidReference = true;
            } else if (orderId != null) {
                Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid order Id: " + orderId));
                payment.setOrder(order);
                amount = order.getTotalAmount();
                hasValidReference = true;
            }
            
            if (!hasValidReference) {
                throw new IllegalArgumentException("At least one of product, service, room, booking, or order must be selected.");
            }
            
            // Set the calculated amount
            payment.setAmountPaid(amount);
            
            // Ensure status is set
            if (payment.getStatus() == null || payment.getStatus().isEmpty()) {
                payment.setStatus("Paid");
            }
            
            // Save the payment
            paymentRepository.save(payment);
            model.addAttribute("successMessage", "Payment saved successfully!");
            return "redirect:/payments";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error saving payment: " + e.getMessage());
            // Re-populate the model with necessary data
            model.addAttribute("products", productRepository.findAll());
            model.addAttribute("services", serviceRepository.findAll());
            model.addAttribute("rooms", roomRepository.findAll());
            model.addAttribute("bookings", bookingRepository.findAll());
            model.addAttribute("orders", orderRepository.findAll());
            return "newPayment";
        }
    }

    // **Update: Mostra form per modificare pagamento**
    @GetMapping("/edit/{id}")
    public String showEditPaymentForm(@PathVariable("id") int id, Model model) {
        Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid payment Id:" + id));
        model.addAttribute("payment", payment);
        // Add products, services, rooms, bookings and orders to the model
        model.addAttribute("products", productRepository.findAll());
        model.addAttribute("services", serviceRepository.findAll());
        model.addAttribute("rooms", roomRepository.findAll());
        model.addAttribute("bookings", bookingRepository.findAll());
        model.addAttribute("orders", orderRepository.findAll());
        return "editPayment"; // Vista Thymeleaf per il form di modifica
    }

    // **Update: Aggiorna pagamento esistente with auto-calculation**
    @PostMapping("/upd")
    public String updatePayment(
            @ModelAttribute("payment") Payment payment,
            @RequestParam(value = "productId", required = false) Integer productId,
            @RequestParam(value = "serviceId", required = false) Integer serviceId,
            @RequestParam(value = "roomId", required = false) Integer roomId,
            @RequestParam(value = "bookingId", required = false) Integer bookingId,
            @RequestParam(value = "orderId", required = false) Integer orderId,
            @RequestParam(value = "quantity", required = false, defaultValue = "1") Integer quantity,
            Model model) {
        try {
            // Default quantity to 1 if not provided
            if (quantity == null || quantity < 1) {
                quantity = 1;
            }
            payment.setQuantity(quantity);
            
            // Calculate the amount based on the selected entity
            BigDecimal amount = null;
            
            // Reset all references first
            payment.setProduct(null);
            payment.setService(null);
            payment.setRoom(null);
            payment.setBooking(null);
            payment.setOrder(null);
            
            if (productId != null) {
                Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid product Id: " + productId));
                payment.setProduct(product);
                amount = product.getPrice().multiply(BigDecimal.valueOf(quantity));
            } else if (serviceId != null) {
                Service service = serviceRepository.findById(serviceId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid service Id: " + serviceId));
                payment.setService(service);
                amount = service.getPrice().multiply(BigDecimal.valueOf(quantity));
            } else if (roomId != null) {
                Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid room Id: " + roomId));
                payment.setRoom(room);
                amount = room.getPrice().multiply(BigDecimal.valueOf(quantity));
            } else if (bookingId != null) {
                Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid booking Id: " + bookingId));
                payment.setBooking(booking);
                
                // Calculate the total price based on the booking room price and duration
                long days = java.time.temporal.ChronoUnit.DAYS.between(
                        booking.getCheckIn(), 
                        booking.getCheckOut()
                );
                days = Math.max(1, days); // Ensure minimum 1 day
                amount = booking.getRoom().getPrice().multiply(BigDecimal.valueOf(days));
            } else if (orderId != null) {
                Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid order Id: " + orderId));
                payment.setOrder(order);
                amount = order.getTotalAmount();
            } else {
                throw new IllegalArgumentException("At least one of product, service, room, booking, or order must be selected.");
            }
            
            // Set the calculated amount
            payment.setAmountPaid(amount);
            
            // Save the payment
            paymentRepository.save(payment);
            model.addAttribute("successMessage", "Payment updated successfully!");
            return "redirect:/payments";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error updating payment: " + e.getMessage());
            // Re-populate the model with necessary data
            model.addAttribute("products", productRepository.findAll());
            model.addAttribute("services", serviceRepository.findAll());
            model.addAttribute("rooms", roomRepository.findAll());
            model.addAttribute("bookings", bookingRepository.findAll());
            model.addAttribute("orders", orderRepository.findAll());
            return "editPayment";
        }
    }

    // **Mark payment as refunded**
    @GetMapping("/refunded/{id}")
    public String markPaymentAsRefunded(@PathVariable("id") int id, Model model) {
        try {
            Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid payment Id:" + id));
            payment.setStatus("Refunded");
            paymentRepository.save(payment);
            model.addAttribute("successMessage", "Payment marked as refunded successfully!");
            return "redirect:/payments";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error marking payment as refunded: " + e.getMessage());
            return "redirect:/payments";
        }
    }

    // **Delete: Elimina pagamento**
    @GetMapping("/delete/{id}")
    public String deletePayment(@PathVariable("id") int id, Model model) {
        try {
            if (paymentRepository.existsById(id)) {
                paymentRepository.deleteById(id);
                model.addAttribute("successMessage", "Payment deleted successfully!");
            }
            return "redirect:/payments";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error deleting payment: " + e.getMessage());
            return "redirect:/payments";
        }
    }

    @PostMapping("/create-from-cart")
    public String createPaymentFromCart(Authentication authentication, Model model) {
        String username = authentication.getName();

        // Retrieve the guest associated with the logged-in user
        // First try to find associated AdmUser
        Optional<AdmUser> userOpt = admUserRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            // If not found by username, try by guest email
            userOpt = admUserRepository.findByGuestEmail(username);
        }

        if (userOpt.isEmpty() || userOpt.get().getGuest() == null) {
            model.addAttribute("errorMessage", "User account not linked to a guest profile. Please contact support.");
            return "redirect:/cart";
        }

        Guest guest = userOpt.get().getGuest();

        // Retrieve all cart items for the guest
        List<Cart> cartItems = cartRepository.findByGuest(guest);
        if (cartItems.isEmpty()) {
            model.addAttribute("errorMessage", "Your cart is empty. Add items before proceeding to payment.");
            return "redirect:/cart";
        }

        // Create a new payment record
        Payment payment = new Payment();
        // Create an order for this payment
        Order order = new Order();
        order.setGuest(guest);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("PENDING");
        order.setTotalAmount(BigDecimal.ZERO); // Will set this later
        orderRepository.save(order);
        
        payment.setOrder(order);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStatus("Pending");

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Cart cartItem : cartItems) {
            BigDecimal itemPrice = cartItem.getProduct() != null
                ? cartItem.getProduct().getPrice()
                : cartItem.getService().getPrice();
            totalAmount = totalAmount.add(itemPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }
        
        order.setTotalAmount(totalAmount);
        orderRepository.save(order);
        
        payment.setAmountPaid(totalAmount);

        // Save the payment record
        paymentRepository.save(payment);

        // Clear the cart after creating the payment
        cartRepository.deleteByGuest(guest);

        model.addAttribute("successMessage", "Payment created successfully!");
        return "redirect:/payments";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException ex, Model model) {
        model.addAttribute("error", "IllegalArgumentException");
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("timestamp", new Date());
        return "error/500";
    }
}