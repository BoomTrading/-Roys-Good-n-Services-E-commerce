package hotel.HotelPackage.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import hotel.HotelPackage.model.Payment;
import hotel.HotelPackage.repository.PaymentRepository;

import java.util.List;

@Controller
@RequestMapping("/payments")
@PreAuthorize("hasRole('ADMIN')") // Solo ADMIN possono accedere
public class PaymentController {

    @Autowired
    private PaymentRepository paymentRepository;

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
        return "newPayment"; // Vista Thymeleaf per il form
    }

    // **Create: Salva nuovo pagamento**
    @PostMapping("/ins")
    public String savePayment(@ModelAttribute("payment") Payment payment, Model model) {
        try {
            paymentRepository.save(payment);
            model.addAttribute("successMessage", "Payment saved successfully!");
            return "redirect:/payments";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error saving payment: " + e.getMessage());
            return "newPayment";
        }
    }

    // **Update: Mostra form per modificare pagamento**
    @GetMapping("/edit/{id}")
    public String showEditPaymentForm(@PathVariable("id") int id, Model model) {
        Payment payment = paymentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid payment Id:" + id));
        model.addAttribute("payment", payment);
        return "editPayment"; // Vista Thymeleaf per il form di modifica
    }

    // **Update: Aggiorna pagamento esistente**
    @PostMapping("/upd")
    public String updatePayment(@ModelAttribute("payment") Payment payment, Model model) {
        try {
            paymentRepository.save(payment);
            model.addAttribute("successMessage", "Payment updated successfully!");
            return "redirect:/payments";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error updating payment: " + e.getMessage());
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