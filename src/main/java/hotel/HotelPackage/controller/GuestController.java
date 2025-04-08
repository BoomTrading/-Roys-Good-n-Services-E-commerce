package hotel.HotelPackage.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.server.ResponseStatusException;

import hotel.HotelPackage.model.Autocomplete;
import hotel.HotelPackage.model.Guest;
import hotel.HotelPackage.repository.GuestRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/guests")
public class GuestController {
    private static final Logger logger = LoggerFactory.getLogger(GuestController.class);

    @Autowired
    private GuestRepository guestRepository;

    @GetMapping("/all")
    public String listGuests(Model model) {
        List<Guest> guests = guestRepository.findAll();
        model.addAttribute("guests", guests);
        return "guests";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String showNewGuestForm(Model model) {
        model.addAttribute("guest", new Guest());
        return "newGuest";
    }

    @PostMapping("/ins")
    @PreAuthorize("hasRole('ADMIN')")
    public String saveGuest(@Valid @ModelAttribute("guest") Guest guest, 
                          BindingResult result, 
                          Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("Not authorized");
        }

        if (result.hasErrors()) {
            logger.warn("Validation errors occurred while saving guest");
            model.addAttribute("errorMessage", "Please correct the form errors");
            return "newGuest";
        }

        if (!isValidGuestData(guest)) {
            logger.warn("Invalid guest data: {}", guest);
            model.addAttribute("errorMessage", "Please provide all required guest information");
            return "newGuest";
        }

        try {
            guestRepository.save(guest);
            logger.info("Guest saved successfully: ID={}", guest.getId());
            return "redirect:/guests/all";
        } catch (DataIntegrityViolationException e) {
            logger.error("Failed to save guest: {}", e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("unique_email")) {
                model.addAttribute("email", guest.getEmail());
                return "error/duplicateEmailError";
            } else {
                model.addAttribute("errorMessage", "A guest with this email already exists");
                return "newGuest";
            }
        }
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditGuestForm(@PathVariable("id") int id, Model model) {
        Guest guest = guestRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid guest Id:" + id));
        model.addAttribute("guest", guest);
        return "editGuest";
    }

    @PostMapping("/upd")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateGuest(@Valid @ModelAttribute("guest") Guest guest, 
                            BindingResult result, 
                            Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (!auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("Not authorized");
        }

        if (result.hasErrors() || !isValidGuestData(guest)) {
            logger.warn("Validation errors occurred while updating guest {}", guest.getId());
            model.addAttribute("errorMessage", "Please correct the form errors");
            return "editGuest";
        }

        try {
            Optional<Guest> existingGuest = guestRepository.findById(guest.getId());
            if (existingGuest.isEmpty()) {
                logger.error("Guest not found for ID: {}", guest.getId());
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Guest not found");
            }

            guestRepository.save(guest);
            logger.info("Guest updated successfully: ID={}", guest.getId());
            return "redirect:/guests/all";
        } catch (DataIntegrityViolationException e) {
            logger.error("Failed to update guest: {}", e.getMessage());
            if (e.getMessage() != null && e.getMessage().contains("unique_email")) {
                model.addAttribute("email", guest.getEmail());
                return "error/duplicateEmailError";
            } else {
                model.addAttribute("errorMessage", "Update failed - duplicate email or invalid data");
                return "editGuest";
            }
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/delete/{id}")
    public String deleteGuest(@PathVariable("id") int id, Model model) {
        try {
            if (guestRepository.existsById(id)) {
                guestRepository.deleteById(id);
                model.addAttribute("successMessage", "Guest deleted successfully!");
            }
            return "redirect:/guests/all";
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("errorMessage", "Cannot delete guest: associated bookings exist.");
            return "error/guestDeleteError";
        }
    }

    @PostMapping("/search")
    public String listGuestsByPatternLike(Model model, @RequestParam String pattern) {
        List<Guest> guests = guestRepository.findByPatternLike(pattern);
        System.out.println("       [pattern: " + pattern + "]");
        model.addAttribute("guests", guests);
        return "guests";
    }

    @GetMapping("/autocomplete")
    @ResponseBody
    public List<Autocomplete> autocomplete(@RequestParam String term) {
        List<Autocomplete> autoList = new ArrayList<>();
        List<Guest> guests = guestRepository.findByPatternLike(term);

        for (Guest guest : guests) {
            Autocomplete item = new Autocomplete();
            item.setLabel(guest.getLastName() + " " + guest.getFirstName());
            item.setValue(guest.getId());
            autoList.add(item);
        }
        return autoList;
    }

    @GetMapping("/details/{id}")
    public String showGuestDetails(@PathVariable("id") int id, Model model) {
        Guest guest = guestRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid guest Id:" + id));
        model.addAttribute("guest", guest);
        return "guestDetails";
    }

    private boolean isValidGuestData(Guest guest) {
        return guest != null 
            && guest.getEmail() != null && !guest.getEmail().trim().isEmpty()
            && guest.getFirstName() != null && !guest.getFirstName().trim().isEmpty()
            && guest.getLastName() != null && !guest.getLastName().trim().isEmpty();
    }
}