package hotel.HotelPackage.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import hotel.HotelPackage.model.AdmUser;
import hotel.HotelPackage.repository.AdmUserRepository;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')") // Solo ADMIN possono accedere
public class AdmUserController {

    @Autowired
    private AdmUserRepository admUserRepository;

    // **Read: Lista di tutti gli utenti admin**
    @GetMapping
    public String listAdmUsers(Model model) {
        List<AdmUser> admUsers = admUserRepository.findAll();
        model.addAttribute("admUsers", admUsers);
        return "adminUsers"; // Vista Thymeleaf per la lista
    }

    // **Create: Mostra form per nuovo admin**
    @GetMapping("/new")
    public String showNewAdmUserForm(Model model) {
        model.addAttribute("admUser", new AdmUser());
        return "newAdmUser"; // Vista Thymeleaf per il form
    }

    // **Create: Salva nuovo admin**
    @PostMapping("/ins")
    public String saveAdmUser(@ModelAttribute("admUser") AdmUser admUser, Model model) {
        try {
            admUserRepository.save(admUser);
            model.addAttribute("successMessage", "Admin user saved successfully!");
            return "redirect:/admin/users";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error saving admin user: " + e.getMessage());
            return "newAdmUser";
        }
    }

    // **Update: Mostra form per modificare admin**
    @GetMapping("/edit/{id}")
    public String showEditAdmUserForm(@PathVariable("id") int id, Model model) {
        AdmUser admUser = admUserRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid admin user Id:" + id));
        model.addAttribute("admUser", admUser);
        return "editAdmUser"; // Vista Thymeleaf per il form di modifica
    }

    // **Update: Aggiorna admin esistente**
    @PostMapping("/upd")
    public String updateAdmUser(@ModelAttribute("admUser") AdmUser admUser, Model model) {
        try {
            admUserRepository.save(admUser);
            model.addAttribute("successMessage", "Admin user updated successfully!");
            return "redirect:/admin/users";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error updating admin user: " + e.getMessage());
            return "editAdmUser";
        }
    }

    // **Delete: Elimina admin**
    @GetMapping("/delete/{id}")
    public String deleteAdmUser(@PathVariable("id") int id, Model model) {
        try {
            if (admUserRepository.existsById(id)) {
                admUserRepository.deleteById(id);
                model.addAttribute("successMessage", "Admin user deleted successfully!");
            }
            return "redirect:/admin/users";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error deleting admin user: " + e.getMessage());
            return "redirect:/admin/users";
        }
    }
}