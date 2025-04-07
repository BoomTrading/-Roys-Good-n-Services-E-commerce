package hotel.HotelPackage.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import hotel.HotelPackage.model.*;
import hotel.HotelPackage.repository.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/payments")
@PreAuthorize("hasRole('ADMIN')") // Solo ADMIN possono accedere
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

    // **Read: Lista di tutti i pagamenti**
    @GetMapping
    public String listPayments(Model model) {
        List<Payment> payments = paymentRepository.findAll();
        model.addAttribute("payments", payments);
        return "payments"; // Vista Thymeleaf per la lista
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
            
            // Calculate the amount based on the selected entity
            BigDecimal amount = null;
            
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
}