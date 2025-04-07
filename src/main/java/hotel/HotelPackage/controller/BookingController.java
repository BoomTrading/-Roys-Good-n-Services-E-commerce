package hotel.HotelPackage.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import hotel.HotelPackage.model.Booking;
import hotel.HotelPackage.model.Guest;
import hotel.HotelPackage.model.Payment;
import hotel.HotelPackage.model.Room;
import hotel.HotelPackage.repository.BookingRepository;
import hotel.HotelPackage.repository.GuestRepository;
import hotel.HotelPackage.repository.RoomRepository;
import hotel.HotelPackage.repository.PaymentRepository;

@Controller
public class BookingController {
    
    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private GuestRepository guestRepository;

    @Autowired
    private RoomRepository roomRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;

    @GetMapping("/")
    public String viewHomePage(@RequestParam(name = "sdate", required = false, defaultValue = "") String sdate, Model model) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String strDate = LocalDateTime.now().minusDays(60).format(formatter);

        List<Map<String, Object>> bookings = bookingRepository.findAllWithGuestRoom(sdate.isEmpty() ? "NOW() - INTERVAL 60 DAY" : sdate);
        model.addAttribute("bookings", bookings);
        model.addAttribute("sdate", sdate.isEmpty() ? strDate : sdate);
        return "index";
    }

    @GetMapping("/test")
    @ResponseBody
    public List<Map<String, Object>> test(@RequestParam(name = "sdate", required = false, defaultValue = "NOW() - INTERVAL 60 DAY") String sdate) {
        return bookingRepository.findAllWithGuestRoom(sdate);
    }

    @GetMapping("/login")
    public String viewLogin() {
        return "login";
    }

    @GetMapping("/bookings/new")
    public String showNewForm(@RequestParam(required = false) Integer roomId, Model model) {
        Booking booking = new Booking();
        
        // If roomId is provided, pre-select the room
        if (roomId != null) {
            Room room = roomRepository.findById(roomId)
                .orElse(null);
            if (room != null) {
                booking.setRoom(room);
                // Set default dates - check-in today, check-out tomorrow
                booking.setCheckIn(LocalDate.now());
                booking.setCheckOut(LocalDate.now().plusDays(1));
                // Calculate default price for one night
                booking.setTotalAmount(room.getPrice());
            }
        }
        
        // Add all guests and available rooms to the model
        model.addAttribute("booking", booking);
        model.addAttribute("guests", guestRepository.findAll());
        model.addAttribute("rooms", roomRepository.findAll());
        return "newBooking";
    }

    @PostMapping("/bookings/ins")
    public String save(@ModelAttribute("booking") Booking booking, Model model) {
        try {
            validateBookingDates(booking, 0);
            bookingRepository.save(booking);
            model.addAttribute("successMessage", "Booking saved successfully!");
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("booking", booking);
            return "newBooking";
        }
    }

    @GetMapping("/bookings/edit/{id}")
    public String showEditForm(@PathVariable("id") int id, Model model) {
        if (bookingRepository.existsById(id)) {
            Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid booking Id:" + id));
            int guestId = booking.getGuest().getId();
            Guest guest = guestRepository.findById(guestId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid guest Id:" + guestId));
            int roomId = booking.getRoom().getId();
            Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid room Id:" + roomId));
            model.addAttribute("booking", booking);
            model.addAttribute("guestName", guest.getLastName() + " " + guest.getFirstName());
            model.addAttribute("roomNumber", room.getRoomNumber() + " - " + room.getType());
            return "editBooking";
        } else {
            return "redirect:/";
        }
    }

    @PostMapping("/bookings/upd")
    public String update(@ModelAttribute("booking") Booking booking, Model model) {
        try {
            validateBookingDates(booking, booking.getId());
            bookingRepository.save(booking);
            model.addAttribute("successMessage", "Booking updated successfully!");
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("booking", booking);
            Room room = booking.getRoom();
            Guest guest = booking.getGuest();
            model.addAttribute("roomNumber", room.getRoomNumber() + " - " + room.getType());
            model.addAttribute("guestName", guest.getLastName() + " " + guest.getFirstName());
            return "editBooking";
        }
    }

    @GetMapping("/bookings/delete/{id}")
    public String delete(@PathVariable("id") int id, Model model) {
        try {
            if (bookingRepository.existsById(id)) {
                bookingRepository.deleteById(id);
                model.addAttribute("successMessage", "Booking deleted successfully!");
            }
            return "redirect:/";
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Check for foreign key constraint violation with payments
            if (e.getMessage() != null && e.getMessage().contains("fk_Payments_Bookings1")) {
                return "error/bookingDeleteError";
            }
            // If it's some other constraint violation, re-throw the exception
            throw e;
        }
    }

    @GetMapping("/bookings/guest/{guestId}")
    public String listBookingsByGuest(@PathVariable("guestId") int guestId, Model model) {
        Guest guest = guestRepository.findById(guestId)
            .orElseThrow(() -> new IllegalArgumentException("Invalid guest Id:" + guestId));
        List<Booking> bookings = bookingRepository.findByGuest(guest);
        model.addAttribute("bookings", bookings);
        model.addAttribute("guestName", guest.getLastName() + " " + guest.getFirstName());
        return "guestBookings";
    }

    @GetMapping("/bookings/calculate")
    @ResponseBody
    public ResponseEntity<?> calculateBookingPrice(
            @RequestParam int roomId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkIn,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkOut) {
        
        System.out.println("Calculate request - roomId: " + roomId + ", checkIn: " + checkIn + ", checkOut: " + checkOut);
        
        try {
            Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + roomId));

            long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
            if (nights <= 0) {
                throw new IllegalArgumentException("Check-out must be after check-in");
            }

            BigDecimal totalPrice = room.getPrice().multiply(BigDecimal.valueOf(nights));
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalAmount", totalPrice);
            response.put("nights", nights);
            response.put("pricePerNight", room.getPrice());
            
            System.out.println("Calculation successful - total: " + totalPrice + ", nights: " + nights);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Calculation error: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    private void validateBookingDates(Booking booking, int excludeBookingId) {
        if (booking.getCheckOut().isBefore(booking.getCheckIn())) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }

        if (bookingRepository.existsOverlappingBooking(
                booking.getRoom().getId(),
                booking.getCheckIn(),
                booking.getCheckOut(),
                excludeBookingId)) {
            throw new IllegalArgumentException("Room is already booked for the selected dates");
        }
    }
    
    @GetMapping("/bookings/all")
    public String getAllBookings(Model model) {
        List<Booking> bookings = bookingRepository.findAll();
        model.addAttribute("bookings", bookings);
        return "bookings";
    }
    
    @GetMapping("/bookings/details/{id}")
    public String getBookingDetails(@PathVariable("id") int id, Model model) {
        // Find the booking or throw exception if not found
        Booking booking = bookingRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid booking Id: " + id));
        
        // Get related payments for this booking
        List<Payment> payments = paymentRepository.findByBooking(booking);
        
        // Calculate payment status
        String paymentStatus = calculatePaymentStatus(booking, payments);
        
        // Add all attributes to the model
        model.addAttribute("booking", booking);
        model.addAttribute("payments", payments);
        model.addAttribute("paymentStatus", paymentStatus);
        
        return "bookingDetails";
    }
    
    private String calculatePaymentStatus(Booking booking, List<Payment> payments) {
        if (payments == null || payments.isEmpty()) {
            return "Unpaid";
        }
        
        // Calculate total amount paid
        BigDecimal totalPaid = payments.stream()
            .filter(p -> "Paid".equals(p.getStatus()))
            .map(Payment::getAmountPaid)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Compare with booking total amount
        if (totalPaid.compareTo(booking.getTotalAmount()) >= 0) {
            return "Paid";
        } else if (totalPaid.compareTo(BigDecimal.ZERO) > 0) {
            return "Partial";
        } else {
            return "Unpaid";
        }
    }
}