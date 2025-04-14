package hotel.HotelPackage.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import hotel.HotelPackage.model.Cart;
import hotel.HotelPackage.model.Guest;
import hotel.HotelPackage.model.Product;
import hotel.HotelPackage.model.Service;
import hotel.HotelPackage.repository.CartRepository;
import hotel.HotelPackage.repository.GuestRepository;
import hotel.HotelPackage.repository.ProductRepository;
import hotel.HotelPackage.repository.ServiceRepository;
import hotel.HotelPackage.repository.AdmUserRepository;
import hotel.HotelPackage.model.AdmUser;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired private CartRepository cartRepository;
    @Autowired private GuestRepository guestRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private ServiceRepository serviceRepository;
    @Autowired private AdmUserRepository admUserRepository;

    // Mostra il carrello corrente
    @GetMapping
    public String showCart(Model model, Authentication authentication) {
        String username = authentication.getName();

        // Se admin, mostra tutti gli articoli del carrello
        if (username.startsWith("admin_")) {
            List<Cart> allItems = cartRepository.findAll();
            BigDecimal total = calculateCartTotal(allItems);
            model.addAttribute("cartItems", allItems);
            model.addAttribute("cartTotal", total);
            model.addAttribute("isAdmin", true);
            return "cart";
        }

        // Recupera l'utente e il relativo guest
        Optional<Guest> guestOpt = getGuestByUsername(username);
        if (guestOpt.isEmpty()) {
            model.addAttribute("errorMessage", "User account not linked to a guest profile. Please contact support.");
            model.addAttribute("cartItems", new ArrayList<Cart>());
            return "cart";
        }

        List<Cart> items = cartRepository.findByGuest(guestOpt.get());
        model.addAttribute("cartItems", items);
        model.addAttribute("cartTotal", calculateCartTotal(items));
        return "cart";
    }

    // Aggiunge un prodotto o servizio al carrello
    @PostMapping("/add")
    public String addToCart(@RequestParam(required = false) Integer productId,
                            @RequestParam(required = false) Integer serviceId,
                            @RequestParam int quantity,
                            @RequestParam(required = false) Integer guestId,
                            Authentication auth, Model model) {
        try {
            String username = auth.getName();
            Guest guest = resolveGuest(username, guestId, model);
            if (guest == null) return "redirect:/cart";

            Cart cartItem = findOrCreateCartItem(guest, productId, serviceId, quantity);
            if (!validateAndAssignItem(cartItem, productId, serviceId, quantity, model)) return "redirect:/cart";

            cartRepository.save(cartItem);
            model.addAttribute("successMessage", "Item added to cart successfully!");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error adding to cart: " + e.getMessage());
        }
        return "redirect:/cart";
    }

    // Rimuove un elemento dal carrello
    @PostMapping("/remove/{id}")
    public String removeFromCart(@PathVariable int id, Model model) {
        try {
            cartRepository.findById(id).ifPresent(cart -> {
                cartRepository.deleteById(id);
                model.addAttribute("successMessage", "Item removed from cart!");
            });
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error removing item: " + e.getMessage());
        }
        return "redirect:/cart";
    }

    // Svuota il carrello di un utente o tutti i carrelli se admin
    @PostMapping("/clear")
    public String clearCart(Authentication auth, @RequestParam(required = false) Integer guestId, Model model) {
        try {
            String username = auth.getName();
            if (username.startsWith("admin_")) {
                if (guestId != null) {
                    Guest guest = guestRepository.findById(guestId)
                        .orElseThrow(() -> new IllegalArgumentException("Guest not found: " + guestId));
                    cartRepository.deleteByGuest(guest);
                    model.addAttribute("successMessage", "Cart cleared for guest: " + guest.getFirstName());
                } else {
                    cartRepository.deleteAll();
                    model.addAttribute("successMessage", "All carts cleared.");
                }
            } else {
                Optional<Guest> guestOpt = guestRepository.findByEmail(username);
                if (guestOpt.isEmpty()) throw new Exception("User not linked to guest profile");
                cartRepository.deleteByGuest(guestOpt.get());
                model.addAttribute("successMessage", "Cart cleared successfully!");
            }
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error clearing cart: " + e.getMessage());
        }
        return "redirect:/cart";
    }

    // Mostra i dettagli di un item nel carrello
    @GetMapping("/details/{id}")
    public String showCartItemDetails(@PathVariable int id, Model model) {
        try {
            Cart item = cartRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid cart item Id: " + id));
            model.addAttribute("cartItem", item);
            return "cartItemDetails";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error retrieving item: " + e.getMessage());
            return "redirect:/cart";
        }
    }

    // Aggiorna la quantità di un item nel carrello
    @PostMapping("/update/{id}")
    public String updateCartItem(@PathVariable int id, @RequestParam int quantity, Model model) {
        try {
            Cart item = cartRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid cart item Id: " + id));
            if (quantity <= 0) throw new IllegalArgumentException("Quantity must be > 0");
            if (item.getProduct() != null && item.getProduct().getStock() < quantity)
                throw new IllegalArgumentException("Insufficient stock for: " + item.getProduct().getName());
            item.setQuantity(quantity);
            cartRepository.save(item);
            model.addAttribute("successMessage", "Item updated!");
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/cart";
    }

    // Mostra il carrello di uno specifico guest (solo per admin)
    @GetMapping("/guest/{guestId}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showGuestCart(@PathVariable int guestId, Model model) {
        try {
            Guest guest = guestRepository.findById(guestId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid guest Id: " + guestId));
            model.addAttribute("cartItems", cartRepository.findByGuest(guest));
            model.addAttribute("guestName", guest.getFirstName() + " " + guest.getLastName());
            model.addAttribute("isAdmin", true);
            return "guestCart";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error retrieving guest cart: " + e.getMessage());
            return "redirect:/cart";
        }
    }

    // Mostra tutti i carrelli (solo admin)
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public String listAllCartItems(Model model) {
        model.addAttribute("cartItems", cartRepository.findAll());
        model.addAttribute("isAdmin", true);
        return "allCarts";
    }

    // Procedi al checkout per un utente normale
    @PostMapping("/checkout")
    public String proceedToCheckout(Authentication auth, Model model) {
        String username = auth.getName();
        if (username.startsWith("admin_")) {
            model.addAttribute("errorMessage", "Admin users cannot checkout.");
            return "redirect:/cart";
        }

        Optional<Guest> guestOpt = getGuestByUsername(username);
        if (guestOpt.isEmpty()) {
            model.addAttribute("errorMessage", "User account not linked to a guest profile.");
            return "redirect:/cart";
        }

        List<Cart> cartItems = cartRepository.findByGuest(guestOpt.get());
        if (cartItems.isEmpty()) {
            model.addAttribute("errorMessage", "Cart is empty.");
            return "redirect:/cart";
        }

        double total = cartItems.stream()
                .mapToDouble(item -> (item.getProduct() != null ? item.getProduct().getPrice().doubleValue() : item.getService().getPrice().doubleValue()) * item.getQuantity())
                .sum();

        model.addAttribute("orderTotal", total);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("guest", guestOpt.get());
        return "checkout";
    }

    // ===== METODI DI SUPPORTO =====

    private BigDecimal calculateCartTotal(List<Cart> items) {
        return items.stream().map(item -> {
            BigDecimal price = item.getProduct() != null ? item.getProduct().getPrice() : item.getService().getPrice();
            return price.multiply(BigDecimal.valueOf(item.getQuantity()));
        }).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Optional<Guest> getGuestByUsername(String username) {
        return admUserRepository.findByUsername(username)
                .or(() -> admUserRepository.findByGuestEmail(username))
                .map(AdmUser::getGuest);
    }

    private Guest resolveGuest(String username, Integer guestId, Model model) {
        if (username.startsWith("admin_")) {
            if (guestId == null) {
                model.addAttribute("errorMessage", "Admin must specify a guest.");
                return null;
            }
            return guestRepository.findById(guestId)
                    .orElseThrow(() -> new IllegalArgumentException("Guest not found: " + guestId));
        } else {
            return getGuestByUsername(username).orElse(null);
        }
    }

    private Cart findOrCreateCartItem(Guest guest, Integer productId, Integer serviceId, int quantity) {
        Cart existing = null;
        if (productId != null) {
            existing = cartRepository.findByGuestAndProduct(guest, productRepository.findById(productId).orElse(null));
        } else if (serviceId != null) {
            existing = cartRepository.findByGuestAndService(guest, serviceRepository.findById(serviceId).orElse(null));
        }

        Cart cart = existing != null ? existing : new Cart();
        if (existing == null) {
            cart.setGuest(guest);
            cart.setAddedAt(LocalDateTime.now());
        }
        cart.setQuantity(existing != null ? existing.getQuantity() + quantity : quantity);
        return cart;
    }

    private boolean validateAndAssignItem(Cart cart, Integer productId, Integer serviceId, int quantity, Model model) {
        if (productId != null) {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
            if (product.getStock() < quantity) {
                model.addAttribute("errorMessage", "Not enough stock for: " + product.getName());
                return false;
            }
            cart.setProduct(product);
        } else if (serviceId != null) {
            Service service = serviceRepository.findById(serviceId)
                    .orElseThrow(() -> new IllegalArgumentException("Service not found: " + serviceId));
            if (!service.isAvailable()) {
                model.addAttribute("errorMessage", "Service not available: " + service.getName());
                return false;
            }
            cart.setService(service);
        } else {
            throw new IllegalArgumentException("Either productId or serviceId must be provided.");
        }
        return true;
    }
}
