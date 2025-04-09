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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    @Autowired
    private AdmUserRepository admUserRepository;

    @GetMapping
    public String showCart(Model model, Authentication authentication) {
        String username = authentication.getName();

        // Check if username starts with "admin_" and show all cart items for admin
        if (username.startsWith("admin_")) {
            List<Cart> allCartItems = cartRepository.findAll();
            
            // Calculate cart total for all items
            BigDecimal cartTotal = BigDecimal.ZERO;
            for (Cart item : allCartItems) {
                BigDecimal itemPrice;
                if (item.getProduct() != null) {
                    itemPrice = item.getProduct().getPrice();
                } else {
                    itemPrice = item.getService().getPrice();
                }
                cartTotal = cartTotal.add(itemPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
            }
            
            model.addAttribute("cartItems", allCartItems);
            model.addAttribute("cartTotal", cartTotal);
            model.addAttribute("isAdmin", true);
            return "cart";
        }

        // For regular users, first try to find associated AdmUser
        Optional<AdmUser> userOpt = admUserRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            // If not found by username, try by guest email
            userOpt = admUserRepository.findByGuestEmail(username);
        }

        if (userOpt.isEmpty() || userOpt.get().getGuest() == null) {
            model.addAttribute("errorMessage", "User account not linked to a guest profile. Please contact support.");
            model.addAttribute("cartItems", new ArrayList<Cart>());
            return "cart";
        }

        Guest guest = userOpt.get().getGuest();
        List<Cart> cartItems = cartRepository.findByGuest(guest);
        
        // Calculate cart total
        BigDecimal cartTotal = BigDecimal.ZERO;
        for (Cart item : cartItems) {
            BigDecimal itemPrice;
            if (item.getProduct() != null) {
                itemPrice = item.getProduct().getPrice();
            } else {
                itemPrice = item.getService().getPrice();
            }
            cartTotal = cartTotal.add(itemPrice.multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("cartTotal", cartTotal);
        return "cart";
    }

    @PostMapping("/add")
    public String addToCart(
            @RequestParam(value = "productId", required = false) Integer productId,
            @RequestParam(value = "serviceId", required = false) Integer serviceId,
            @RequestParam("quantity") int quantity,
            @RequestParam(value = "guestId", required = false) Integer guestId,
            Authentication authentication,
            Model model) {
        try {
            String username = authentication.getName();
            Guest guest;

            // Regular user flow
            if (!username.startsWith("admin_")) {
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
                guest = userOpt.get().getGuest();
            } else {
                // Admin flow - must specify a guest
                if (guestId == null) {
                    model.addAttribute("errorMessage", "Admin must specify a guest when adding items to cart");
                    return "redirect:/cart";
                }
                guest = guestRepository.findById(guestId)
                        .orElseThrow(() -> new IllegalArgumentException("Guest not found: " + guestId));
            }

            // Rest of the method remains unchanged
            Cart existingItem = null;
            if (productId != null) {
                existingItem = cartRepository.findByGuestAndProduct(guest,
                        productRepository.findById(productId).orElse(null));
            } else if (serviceId != null) {
                existingItem = cartRepository.findByGuestAndService(guest,
                        serviceRepository.findById(serviceId).orElse(null));
            }

            Cart cartItem = existingItem != null ? existingItem : new Cart();
            if (existingItem == null) {
                cartItem.setGuest(guest);
                cartItem.setAddedAt(LocalDateTime.now());
            }

            int newQuantity = existingItem != null ? existingItem.getQuantity() + quantity : quantity;
            cartItem.setQuantity(newQuantity);

            if (productId != null) {
                Product product = productRepository.findById(productId)
                        .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));
                if (product.getStock() < newQuantity) {
                    model.addAttribute("errorMessage", "Not enough stock for product: " + product.getName());
                    return "redirect:/products/details/" + productId;
                }
                cartItem.setProduct(product);
            } else if (serviceId != null) {
                Service service = serviceRepository.findById(serviceId)
                        .orElseThrow(() -> new IllegalArgumentException("Service not found: " + serviceId));
                if (!service.isAvailable()) {
                    model.addAttribute("errorMessage", "Service not available: " + service.getName());
                    return "redirect:/services/details/" + serviceId;
                }
                cartItem.setService(service);
            } else {
                throw new IllegalArgumentException("Either productId or serviceId must be provided.");
            }

            cartRepository.save(cartItem);
            model.addAttribute("successMessage", "Item added to cart successfully!");
            return "redirect:/cart";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error adding to cart: " + e.getMessage());
            if (productId != null) {
                return "redirect:/products/details/" + productId;
            } else if (serviceId != null) {
                return "redirect:/services/details/" + serviceId;
            }
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
    public String clearCart(Authentication authentication,
            @RequestParam(value = "guestId", required = false) Integer guestId,
            Model model) {
        try {
            String username = authentication.getName();

            // Admin can clear a specific guest's cart
            if (username.startsWith("admin_") && guestId != null) {
                Guest guest = guestRepository.findById(guestId)
                        .orElseThrow(() -> new IllegalArgumentException("Guest not found: " + guestId));
                cartRepository.deleteByGuest(guest);
                model.addAttribute("successMessage", "Cart cleared successfully for guest: " +
                        guest.getFirstName() + " " + guest.getLastName());
                return "redirect:/cart";
            }
            // Admin wants to clear all carts (system-wide)
            else if (username.startsWith("admin_") && guestId == null) {
                cartRepository.deleteAll();
                model.addAttribute("successMessage", "All carts cleared from the system");
                return "redirect:/cart";
            }

            // Regular user clears their own cart
            Optional<Guest> guestOpt = guestRepository.findByEmail(username);
            if (guestOpt.isEmpty()) {
                model.addAttribute("errorMessage", "User account not linked to a guest profile");
                return "redirect:/cart";
            }

            Guest guest = guestOpt.get();
            cartRepository.deleteByGuest(guest);
            model.addAttribute("successMessage", "Cart cleared successfully!");
            return "redirect:/cart";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error clearing cart: " + e.getMessage());
            return "redirect:/cart";
        }
    }

    @GetMapping("/details/{id}")
    public String showCartItemDetails(@PathVariable("id") int id, Model model) {
        try {
            Cart cartItem = cartRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid cart item Id:" + id));
            model.addAttribute("cartItem", cartItem);
            return "cartItemDetails";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error retrieving cart item details: " + e.getMessage());
            return "redirect:/cart";
        }
    }

    @PostMapping("/update/{id}")
    public String updateCartItem(@PathVariable("id") int id,
            @RequestParam("quantity") int quantity,
            Model model) {
        try {
            Cart cartItem = cartRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid cart item Id:" + id));

            // Validate quantity
            if (quantity <= 0) {
                model.addAttribute("errorMessage", "Quantity must be greater than zero");
                return "redirect:/cart/details/" + id;
            }

            // Check stock if it's a product
            if (cartItem.getProduct() != null && cartItem.getProduct().getStock() < quantity) {
                model.addAttribute("errorMessage", "Not enough stock for product: " +
                        cartItem.getProduct().getName());
                return "redirect:/cart/details/" + id;
            }

            cartItem.setQuantity(quantity);
            cartRepository.save(cartItem);
            model.addAttribute("successMessage", "Cart item updated successfully!");
            return "redirect:/cart";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error updating cart item: " + e.getMessage());
            return "redirect:/cart";
        }
    }

    @GetMapping("/guest/{guestId}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showGuestCart(@PathVariable("guestId") int guestId, Model model) {
        try {
            Guest guest = guestRepository.findById(guestId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid guest Id:" + guestId));
            List<Cart> cartItems = cartRepository.findByGuest(guest);
            model.addAttribute("cartItems", cartItems);
            model.addAttribute("guestName", guest.getFirstName() + " " + guest.getLastName());
            model.addAttribute("isAdmin", true);
            return "guestCart";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error retrieving guest cart: " + e.getMessage());
            return "redirect:/cart";
        }
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public String listAllCartItems(Model model) {
        List<Cart> cartItems = cartRepository.findAll();
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("isAdmin", true);
        return "allCarts";
    }

    @PostMapping("/checkout")
    public String proceedToCheckout(Authentication authentication, Model model) {
        String username = authentication.getName();
        
        // Admin users don't have access to the checkout page
        if (username.startsWith("admin_")) {
            model.addAttribute("errorMessage", "Admin users cannot checkout. Please login as a regular user.");
            return "redirect:/cart";
        }

        // For regular users, use the same guest lookup logic as in showCart method
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
        List<Cart> cartItems = cartRepository.findByGuest(guest);
        
        if (cartItems.isEmpty()) {
            model.addAttribute("errorMessage", "Your cart is empty. Add items before proceeding to checkout.");
            return "redirect:/cart";
        }

        // Calculate total in controller
        double orderTotal = 0.0;
        for (Cart item : cartItems) {
            double price = item.getProduct() != null ? item.getProduct().getPrice().doubleValue() : item.getService().getPrice().doubleValue();
            orderTotal += price * item.getQuantity();
        }
        model.addAttribute("orderTotal", orderTotal);

        // Pass cart items and guest information to the checkout template
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("guest", guest);
        return "checkout";
    }
}