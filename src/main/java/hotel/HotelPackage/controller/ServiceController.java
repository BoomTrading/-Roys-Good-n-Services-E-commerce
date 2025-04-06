package hotel.HotelPackage.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import hotel.HotelPackage.model.Service;
import hotel.HotelPackage.repository.ServiceRepository;

import java.util.List;

@Controller
@RequestMapping("/services")
public class ServiceController {

    @Autowired
    private ServiceRepository serviceRepository;

    // List all services (existing endpoint for the general Services page)
    @GetMapping("/all")
    public String listServices(Model model) {
        List<Service> services = serviceRepository.findAll();
        List<String> categories = serviceRepository.findDistinctCategories();
        model.addAttribute("services", services);
        model.addAttribute("categories", categories);
        return "services";
    }

    // List services by category (used for filtering in the general Services page)
    @GetMapping("/category/{category}")
    public String listServicesByCategory(@PathVariable("category") String category, Model model) {
        List<Service> services = serviceRepository.findByCategory(category);
        List<String> categories = serviceRepository.findDistinctCategories();
        model.addAttribute("services", services);
        model.addAttribute("categories", categories);
        model.addAttribute("currentCategory", category);
        return "services";
    }

    // Dedicated endpoint for Spa page (filters services with category 'Spa')
    @GetMapping("/spa")
    public String listSpaServices(Model model) {
        List<Service> spas = serviceRepository.findByCategory("Spa");
        model.addAttribute("spas", spas);
        return "spa";
    }

    // Dedicated endpoint for Restaurants page (filters services with category
    // 'Restaurants')
    @GetMapping("/restaurants")
    public String listRestaurantServices(Model model) {
        List<Service> restaurants = serviceRepository.findByCategory("Restaurants");
        model.addAttribute("restaurants", restaurants);
        return "restaurants";
    }

    // New endpoint for Helicopter
    @GetMapping("/helicopter")
    public String listHelicopterServices(Model model) {
        List<Service> helicopters = serviceRepository.findByCategory("Helicopter");
        model.addAttribute("helicopters", helicopters);
        return "helicopter";
    }

    @GetMapping("/privatejet")
    public String listPrivateJetServices(Model model) {
        List<Service> privateJets = serviceRepository.findByCategory("Private Jet");
        model.addAttribute("privateJets", privateJets);
        return "privatejet";
    }
    // New endpoint for Yacht
    @GetMapping("/yacht")
    public String listYachtServices(Model model) {
        List<Service> yachts = serviceRepository.findByCategory("Yacht");
        model.addAttribute("yachts", yachts);
        return "yacht";
    }
     // New endpoint for Golf
     @GetMapping("/golf")
     public String listGolfServices(Model model) {
         List<Service> golfServices = serviceRepository.findByCategory("Golf");
         model.addAttribute("golfServices", golfServices);
         return "golf";
     }

    // Show form to create a new service (used for both Spa and Restaurants)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/new")
    public String showNewServiceForm(Model model) {
        model.addAttribute("service", new Service());
        List<String> categories = serviceRepository.findDistinctCategories();
        model.addAttribute("categories", categories);
        return "newService";
    }

    // Save a new service (used for both Spa and Restaurants)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/ins")
    public String saveService(@ModelAttribute("service") Service service, Model model) {
        try {
            serviceRepository.save(service);
            model.addAttribute("successMessage", "Service saved successfully!");
            // Redirect based on the category of the new service
            if ("Spa".equalsIgnoreCase(service.getCategory())) {
                return "redirect:/services/spa";
            } else if ("Restaurants".equalsIgnoreCase(service.getCategory())) {
                return "redirect:/services/restaurants";
            } else if ("Helicopter".equalsIgnoreCase(service.getCategory())) {
                return "redirect:/services/helicopter";
            } else if ("Private Jet".equalsIgnoreCase(service.getCategory())) {
                return "redirect:/services/privatejet";
            } else if ("Yacht".equalsIgnoreCase(service.getCategory())) {
                return "redirect:/services/yacht";
            } else if ("Golf".equalsIgnoreCase(service.getCategory())) {
                return "redirect:/services/golf";
            }
            return "redirect:/services/all";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error saving service: " + e.getMessage());
            List<String> categories = serviceRepository.findDistinctCategories();
            model.addAttribute("categories", categories);
            return "newService";
        }
    }

    // Show form to edit a service (used for both Spa and Restaurants)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/edit/{id}")
    public String showEditServiceForm(@PathVariable("id") int id, Model model) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid service Id: " + id));
        List<String> categories = serviceRepository.findDistinctCategories();
        model.addAttribute("service", service);
        model.addAttribute("categories", categories);
        return "editService";
    }

    // Update an existing service (used for both Spa and Restaurants)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/upd")
    public String updateService(@ModelAttribute("service") Service service, Model model) {
        try {
            serviceRepository.save(service);
            model.addAttribute("successMessage", "Service updated successfully!");
            // Redirect based on the category of the updated service
            if ("Spa".equalsIgnoreCase(service.getCategory())) {
                return "redirect:/services/spa";
            } else if ("Restaurants".equalsIgnoreCase(service.getCategory())) {
                return "redirect:/services/restaurants";
            } else if ("Helicopter".equalsIgnoreCase(service.getCategory())) {
                return "redirect:/services/helicopter";
            } else if ("Private Jet".equalsIgnoreCase(service.getCategory())) {
                return "redirect:/services/privatejet";
            } else if ("Yacht".equalsIgnoreCase(service.getCategory())) {
                return "redirect:/services/yacht";
            } else if ("Golf".equalsIgnoreCase(service.getCategory())) {
                return "redirect:/services/golf";
            }
            return "redirect:/services/all";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error updating service: " + e.getMessage());
            List<String> categories = serviceRepository.findDistinctCategories();
            model.addAttribute("categories", categories);
            return "editService";
        }
    }

    // Delete a service (used for both Spa and Restaurants)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/delete/{id}")
    public String deleteService(@PathVariable("id") int id, Model model) {
        try {
            Service service = serviceRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid service Id: " + id));
            serviceRepository.deleteById(id);
            model.addAttribute("successMessage", "Service deleted successfully!");
            // Redirect based on the category of the deleted service
            if ("Spa".equalsIgnoreCase(service.getCategory())) {
                return "redirect:/services/spa";
            } else if ("Restaurants".equalsIgnoreCase(service.getCategory())) {
                return "redirect:/services/restaurants";
            } else if ("Helicopter".equalsIgnoreCase(service.getCategory())) {
                return "redirect:/services/helicopter";
            } else if ("Private Jet".equalsIgnoreCase(service.getCategory())) {
                return "redirect:/services/privatejet";
            } else if ("Yacht".equalsIgnoreCase(service.getCategory())) {
                return "redirect:/services/yacht";
            } else if ("Golf".equalsIgnoreCase(service.getCategory())) {
                return "redirect:/services/golf";
            }
            return "redirect:/services/all";
        } catch (Exception e) {
            Service service = serviceRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid service Id: " + id));
            model.addAttribute("service", service);
            model.addAttribute("errorMessage", "Cannot delete service: associated bookings exist.");
            return "error/serviceDeleteError";
        }
    }

    // Search services (used for the general Services page)
    @PostMapping("/search")
    public String listServicesByPatternLike(Model model, @RequestParam String pattern) {
        List<Service> services = serviceRepository.findByPatternLike(pattern);
        List<String> categories = serviceRepository.findDistinctCategories();
        model.addAttribute("services", services);
        model.addAttribute("categories", categories);
        return "services";
    }

    // Show details of a service (used for both Spa and Restaurants)
    @GetMapping("/details/{id}")
    public String showServiceDetails(@PathVariable("id") int id, Model model) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid service Id: " + id));
        model.addAttribute("service", service);
        return "serviceDetails";
    }
}