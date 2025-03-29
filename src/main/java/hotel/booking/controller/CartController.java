package hotel.booking.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import hotel.booking.model.Cart;
import hotel.booking.model.Guest;
import hotel.booking.model.Product;
import hotel.booking.model.Service;
import hotel.booking.repository.CartRepository;
import hotel.booking.repository.GuestRepository;
import hotel.booking.repository.ProductRepository;
import hotel.booking.repository.ServiceRepository;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @GetMapping
    public String showCart(Model model, Authentication authentication) {
        String email = authentication.getName();
        Guest guest = guestRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Guest not found: " + email));
        List<Cart> cartItems = cartRepository.findByGuest(guest);
        model.addAttribute("cartItems", cartItems);
        return "cart";
    }

    @PostMapping("/add")
    public String addToCart(
            @RequestParam(value = "productId", required = false) Integer productId,
            @RequestParam(value = "serviceId", required = false) Integer serviceId,
            @RequestParam("quantity") int quantity,
            Authentication authentication,
            Model model) {
        try {
            String email = authentication.getName();
            Guest guest = guestRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Guest not found: " + email));

            Cart cartItem = new Cart();
            cartItem.setGuest(guest);
            cartItem.setQuantity(quantity);
            cartItem.setAddedAt(LocalDateTime.now());

            if (productId != null) {
                Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
                if (product.getStock() < quantity) {
                    model.addAttribute("errorMessage", "Not enough stock for product: " + product.getName());
                    return "redirect:/products/all";
                }
                cartItem.setProduct(product);
            } else if (serviceId != null) {
                Service service = serviceRepository.findById(serviceId)
                    .orElseThrow(() -> new IllegalArgumentException("Service not found: " + serviceId));
                if (!service.isAvailable()) {
                    model.addAttribute("errorMessage", "Service not available: " + service.getName());
                    return "redirect:/services/all";
                }
                cartItem.setService(service);
            } else {
                throw new IllegalArgumentException("Either productId or serviceId must be provided.");
            }

            cartRepository.save(cartItem);
            model.addAttribute("successMessage", "Item added to cart!");
            return "redirect:/cart";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error adding to cart: " + e.getMessage());
            return "redirect:/cart";
        }
    }

    @PostMapping("/remove/{id}")
    public String removeFromCart(@PathVariable("id") int id, Model model) {
        try {
            if (cartRepository.existsById(id)) {
                cartRepository.deleteById(id);
                model.addAttribute("successMessage", "Item removed from cart!");
            }
            return "redirect:/cart";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error removing item: " + e.getMessage());
            return "redirect:/cart";
        }
    }

    @PostMapping("/clear")
    public String clearCart(Authentication authentication, Model model) {
        try {
            String email = authentication.getName();
            Guest guest = guestRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Guest not found: " + email));
            cartRepository.deleteByGuest(guest);
            model.addAttribute("successMessage", "Cart cleared successfully!");
            return "redirect:/cart";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error clearing cart: " + e.getMessage());
            return "redirect:/cart";
        }
    }
}