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

    @GetMapping("/all")
    public String listServices(Model model) {
        List<Service> services = serviceRepository.findAll();
        List<String> categories = serviceRepository.findDistinctCategories();
        model.addAttribute("services", services);
        model.addAttribute("categories", categories);
        return "services";
    }

    @GetMapping("/category/{category}")
    public String listServicesByCategory(@PathVariable("category") String category, Model model) {
        List<Service> services = serviceRepository.findByCategory(category);
        List<String> categories = serviceRepository.findDistinctCategories();
        model.addAttribute("services", services);
        model.addAttribute("categories", categories);
        model.addAttribute("currentCategory", category);
        return "services";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/new")
    public String showNewServiceForm(Model model) {
        model.addAttribute("service", new Service());
        // Add available categories to help with form selection
        List<String> categories = serviceRepository.findDistinctCategories();
        model.addAttribute("categories", categories);
        return "newService";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/ins")
    public String saveService(@ModelAttribute("service") Service service, Model model) {
        try {
            serviceRepository.save(service);
            model.addAttribute("successMessage", "Service saved successfully!");
            return "redirect:/services/all";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error saving service: " + e.getMessage());
            return "newService";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditServiceForm(@PathVariable("id") int id, Model model) {
        Service service = serviceRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid service Id:" + id));
        List<String> categories = serviceRepository.findDistinctCategories();
        model.addAttribute("service", service);
        model.addAttribute("categories", categories);
        return "editService";
    }

    @PostMapping("/upd")
    public String updateService(@ModelAttribute("service") Service service, Model model) {
        try {
            serviceRepository.save(service);
            model.addAttribute("successMessage", "Service updated successfully!");
            return "redirect:/services/all";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error updating service: " + e.getMessage());
            return "editService";
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/delete/{id}")
    public String deleteService(@PathVariable("id") int id, Model model) {
        try {
            if (serviceRepository.existsById(id)) {
                serviceRepository.deleteById(id);
                model.addAttribute("successMessage", "Service deleted successfully!");
            }
            return "redirect:/services/all";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Cannot delete service: associated orders exist.");
            return "error/serviceDeleteError";
        }
    }

    @PostMapping("/search")
    public String listServicesByPatternLike(Model model, @RequestParam String pattern) {
        List<Service> services = serviceRepository.findByPatternLike(pattern);
        List<String> categories = serviceRepository.findDistinctCategories();
        model.addAttribute("services", services);
        model.addAttribute("categories", categories);
        return "services";
    }

    @GetMapping("/details/{id}")
    public String showServiceDetails(@PathVariable("id") int id, Model model) {
        Service service = serviceRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid service Id: " + id));
        model.addAttribute("service", service);
        return "serviceDetails";
    }
}