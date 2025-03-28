package hotel.booking.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import hotel.booking.model.Autocomplete;
import hotel.booking.model.Guest;
import hotel.booking.repository.GuestRepository;

import java.util.ArrayList;
import java.util.List;


@Controller
@RequestMapping("/guests")
public class GuestController {

	@Autowired
	private GuestRepository guestRepository;

	@GetMapping("/all")
	public String listGuests(Model model) {
		List<Guest> guests = guestRepository.findAll();
		model.addAttribute("guests", guests);
		return "guests";
	}

	@GetMapping("/new")
	public String showNewGuestForm(Model model) {
		model.addAttribute("guest", new Guest());
		return "newGuest";
	}

	@PostMapping("/ins")
	public String saveGuest(@ModelAttribute("guest") Guest guest) {
		guestRepository.save(guest);
		return "redirect:/guests/all";
	}

	@GetMapping("/edit/{id}")
	public String showEditGuestForm(@PathVariable("id") int id, Model model) {
		Guest guest = guestRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid guest Id:" + id));
		model.addAttribute("guest", guest);
		return "editGuest";
	}

	@PostMapping("/upd")
	public String updateGuest(@ModelAttribute("guest") Guest guest) {
		guestRepository.save(guest);
		return "redirect:/guests/all";
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/delete/{id}")
	public String deleteGuest(@PathVariable("id") int id) {
		try {
			if(guestRepository.existsById(id)) {
				guestRepository.deleteById(id);
			}
			return "redirect:/guests/all";
		} catch (DataIntegrityViolationException e) {
			return "error/guestDeleteError";
		}
	}

	@PostMapping("/search")
	public String listGuestsByPatternLike(Model model, @RequestParam String pattern) {
	List<Guest> guests = guestRepository.findByPatternLike(pattern);
	System.out.println("       [pattern: "+pattern +"]");
		model.addAttribute("guests", guests);
		return "guests";
	}
	
	@GetMapping("/autocomplete")
	@ResponseBody
	public List<Autocomplete> autocomplete(@RequestParam String term) {
		List<Autocomplete> autoList = new ArrayList<Autocomplete>();
		List<Guest> guests = guestRepository.findByPatternLike(term);

		for (Guest guest : guests) {
			Autocomplete item = new Autocomplete();
			item.setLabel(guest.getLastName() +" "+ guest.getFirstName());
			item.setValue(guest.getId());
			autoList.add(item);
		}
		return autoList;
	}

	
	// Add more CRUD methods for each entity as needed
}
