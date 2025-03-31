package hotel.HotelPackage.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import hotel.HotelPackage.model.Autocomplete;
import hotel.HotelPackage.model.Room;
import hotel.HotelPackage.repository.BookingRepository;
import hotel.HotelPackage.repository.RoomRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;

@Controller
@RequestMapping("/rooms")
public class RoomController {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    // Add a redirect for the /rooms path
    @GetMapping("")
    public String redirectToRoomsList() {
        return "redirect:/rooms/all";
    }

    @GetMapping("/all")
    public String listRooms(Model model, @RequestParam(required = false) String sortType) {
        List<Room> rooms = roomRepository.findAll();
        if (sortType != null && !sortType.isEmpty()) {
            rooms = rooms.stream()
                .filter(room -> room.getType().toString().equals(sortType))
                .collect(Collectors.toList());
        }
        model.addAttribute("rooms", rooms);
        model.addAttribute("roomTypes", Room.RoomType.values());
        return "rooms";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/new")
    public String showNewRoomForm(Model model) {
        model.addAttribute("room", new Room());
        return "newRoom";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/ins")
    public String saveRoom(@ModelAttribute("room") Room room, Model model) {
        try {
            if (roomRepository.existsByRoomNumber(room.getRoomNumber())) {
                model.addAttribute("errorMessage", "Room number already exists.");
                return "newRoom";
            }
            roomRepository.save(room);
            model.addAttribute("successMessage", "Room saved successfully!");
            return "redirect:/rooms/all";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error saving room: " + e.getMessage());
            return "newRoom";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditRoomForm(@PathVariable("id") int id, Model model) {
        Room room = roomRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid room Id:" + id));
        model.addAttribute("room", room);
        return "editRoom";
    }

    @PostMapping("/upd")
    public String updateRoom(@ModelAttribute("room") Room room, Model model) {
        try {
            if (roomRepository.existsByRoomNumberAndIdNot(room.getRoomNumber(), room.getId())) {
                model.addAttribute("errorMessage", "Room number already exists.");
                return "editRoom";
            }
            roomRepository.save(room);
            model.addAttribute("successMessage", "Room updated successfully!");
            return "redirect:/rooms/all";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error updating room: " + e.getMessage());
            return "editRoom";
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/delete/{id}")
    public String deleteRoom(@PathVariable("id") int id, Model model) {
        try {
            if (roomRepository.existsById(id)) {
                roomRepository.deleteById(id);
                model.addAttribute("successMessage", "Room deleted successfully!");
            }
            return "redirect:/rooms/all";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Cannot delete room: associated bookings exist.");
            return "error/roomDeleteError";
        }
    }

    @PostMapping("/search")
    public String listRoomsByPatternLike(Model model, @RequestParam String pattern) {
        List<Room> rooms = roomRepository.findByPatternLike(pattern);
        model.addAttribute("rooms", rooms);
        return "rooms";
    }

    @GetMapping("/autocomplete")
    @ResponseBody
    public List<Autocomplete> autocomplete(@RequestParam String term) {
        List<Autocomplete> autoList = new ArrayList<>();
        List<Room> rooms = roomRepository.findByPatternLike(term);
        for (Room room : rooms) {
            Autocomplete item = new Autocomplete();
            item.setLabel(room.getRoomNumber() + " - " + room.getType());
            item.setValue(room.getId());
            autoList.add(item);
        }
        return autoList;
    }

    @GetMapping("/available")
    @ResponseBody
    public List<RoomDTO> getAvailableRooms(
            @RequestParam Room.RoomType roomType,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkIn,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkOut,
            @RequestParam(required = false, defaultValue = "0") Integer currentBookingId) {
        
        List<Room> availableRooms = roomRepository.findAvailableRoomsByType(roomType);
        return availableRooms.stream()
            .filter(room -> !bookingRepository.existsOverlappingBooking(
                room.getId(), checkIn, checkOut, currentBookingId))
            .map(room -> new RoomDTO(room.getId(), room.getRoomNumber(), room.getPrice()))
            .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public String showRoomDetails(@PathVariable("id") int id, Model model) {
        Room room = roomRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid room Id:" + id));
        model.addAttribute("room", room);
        return "roomDetails";
    }
}

class RoomDTO {
    private int id;
    private String roomNumber;
    private BigDecimal price;

    public RoomDTO(int id, String roomNumber, BigDecimal price) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.price = price;
    }

    public int getId() { return id; }
    public String getRoomNumber() { return roomNumber; }
    public BigDecimal getPrice() { return price; }
}