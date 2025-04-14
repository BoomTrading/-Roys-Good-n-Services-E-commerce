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
@PreAuthorize("hasRole('ADMIN')") // Accesso consentito solo agli utenti con ruolo ADMIN
public class AdmUserController {

    @Autowired
    private AdmUserRepository admUserRepository;

    // === READ: Visualizza tutti gli utenti admin ===
    @GetMapping
    public String listAdmUsers(Model model) {
        List<AdmUser> admUsers = admUserRepository.findAll(); // Recupera tutti gli admin
        model.addAttribute("admUsers", admUsers);              // Aggiunge lista al modello
        return "adminUsers";                                   // Ritorna la vista Thymeleaf
    }

    // === CREATE: Mostra il form per creare un nuovo admin ===
    @GetMapping("/new")
    public String showNewAdmUserForm(Model model) {
        model.addAttribute("admUser", new AdmUser()); // Aggiunge oggetto vuoto al modello
        return "newAdmUser";                          // Vista del form di inserimento
    }

    // === CREATE: Salva il nuovo admin nel database ===
    @PostMapping("/ins")
    public String saveAdmUser(@ModelAttribute("admUser") AdmUser admUser, Model model) {
        try {
            admUserRepository.save(admUser); // Salva nel database
            return "redirect:/admin/users";  // Redirect alla lista utenti
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Errore durante il salvataggio: " + e.getMessage());
            return "newAdmUser";             // Ritorna alla form in caso di errore
        }
    }

    // === UPDATE: Mostra il form per modificare un admin esistente ===
    @GetMapping("/edit/{id}")
    public String showEditAdmUserForm(@PathVariable("id") int id, Model model) {
        AdmUser admUser = admUserRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("ID utente non valido: " + id));
        model.addAttribute("admUser", admUser); // Aggiunge utente esistente al modello
        return "editAdmUser";                  // Vista del form di modifica
    }

    // === UPDATE: Aggiorna i dati dell'admin ===
    @PostMapping("/upd")
    public String updateAdmUser(@ModelAttribute("admUser") AdmUser admUser, Model model) {
        try {
            admUserRepository.save(admUser);  // Salva modifiche
            return "redirect:/admin/users";   // Redirect alla lista utenti
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Errore durante l'aggiornamento: " + e.getMessage());
            return "editAdmUser";             // Ritorna al form in caso di errore
        }
    }

    // === DELETE: Elimina un admin ===
    @GetMapping("/delete/{id}")
    public String deleteAdmUser(@PathVariable("id") int id, Model model) {
        try {
            if (admUserRepository.existsById(id)) {
                admUserRepository.deleteById(id); // Elimina se esiste
            }
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Errore durante l'eliminazione: " + e.getMessage());
        }
        return "redirect:/admin/users"; // Redirect in ogni caso alla lista
    }
}
