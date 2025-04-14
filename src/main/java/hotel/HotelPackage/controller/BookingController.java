package hotel.HotelPackage.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    // Mostra la homepage con le prenotazioni recenti (ultimi 60 giorni)
    @GetMapping("/")
    public String viewHomePage(@RequestParam(name = "sdate", required = false, defaultValue = "") String sdate, Model model) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String strDate = LocalDateTime.now().minusDays(60).format(formatter);

        List<Map<String, Object>> bookings = bookingRepository.findAllWithGuestRoom(
            sdate.isEmpty() ? "NOW() - INTERVAL 60 DAY" : sdate);
        
        model.addAttribute("bookings", bookings);
        model.addAttribute("sdate", sdate.isEmpty() ? strDate : sdate);
        return "index";
    }

    // Endpoint test per verificare le prenotazioni con dati aggregati
    @GetMapping("/test")
    @ResponseBody
    public List<Map<String, Object>> test(@RequestParam(name = "sdate", required = false, defaultValue = "NOW() - INTERVAL 60 DAY") String sdate) {
        return bookingRepository.findAllWithGuestRoom(sdate);
    }

    // Mostra la pagina di login
    @GetMapping("/login")
    public String viewLogin() {
        return "login";
    }

    // Mostra il form per una nuova prenotazione (utente o admin)
    @GetMapping("/bookings/new")
    public String showNewForm(@RequestParam(required = false) Integer roomId, Model model) {
        Booking booking = new Booking();

        if (roomId != null) {
            roomRepository.findById(roomId).ifPresent(room -> {
                booking.setRoom(room);
                booking.setCheckIn(LocalDate.now());
                booking.setCheckOut(LocalDate.now().plusDays(1));
                booking.setTotalAmount(room.getPrice());
            });
        }

        if (Boolean.TRUE.equals(model.getAttribute("isAdmin"))) {
            model.addAttribute("booking", booking);
            model.addAttribute("guests", guestRepository.findAll());
            model.addAttribute("rooms", roomRepository.findAll());
            return "newBooking";
        } else {
            model.addAttribute("guest", new Guest());
            model.addAttribute("booking", booking);
            model.addAttribute("rooms", roomRepository.findAll());
            return "userBooking";
        }
    }

    // Salva una prenotazione da parte di un admin
    @PostMapping("/bookings/ins")
    public String save(@ModelAttribute("booking") Booking booking, Model model) {
        try {
            validateBookingDates(booking, 0);
            bookingRepository.save(booking);
            model.addAttribute("successMessage", "Prenotazione salvata con successo!");
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("booking", booking);
            return "newBooking";
        }
    }

    // Salva una prenotazione con nuovo guest (utente normale)
    @PostMapping("/bookings/user-ins")
    public String saveUserBooking(@ModelAttribute("guest") Guest guest,
                                  @ModelAttribute("booking") Booking booking,
                                  @RequestParam("roomId") int roomId,
                                  Model model) {
        try {
            Guest savedGuest = guestRepository.save(guest);
            Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("ID stanza non valido: " + roomId));

            booking.setGuest(savedGuest);
            booking.setRoom(room);
            validateBookingDates(booking, 0);
            Booking savedBooking = bookingRepository.save(booking);

            return "redirect:/bookings/details/" + savedBooking.getId() + "?newBooking=true";
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage() != null && e.getMessage().contains("unique_email")) {
                model.addAttribute("email", guest.getEmail());
                return "error/duplicateEmailError";
            }
            model.addAttribute("errorMessage", "Errore nel salvataggio. Verifica i dati.");
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }

        model.addAttribute("guest", guest);
        model.addAttribute("booking", booking);
        model.addAttribute("rooms", roomRepository.findAll());
        model.addAttribute("selectedRoomId", roomId);
        return "userBooking";
    }

    // Mostra il form per modificare una prenotazione
    @GetMapping("/bookings/edit/{id}")
    public String showEditForm(@PathVariable("id") int id, Model model) {
        return bookingRepository.findById(id).map(booking -> {
            Guest guest = booking.getGuest();
            Room room = booking.getRoom();
            model.addAttribute("booking", booking);
            model.addAttribute("guestName", guest.getLastName() + " " + guest.getFirstName());
            model.addAttribute("roomNumber", room.getRoomNumber() + " - " + room.getType());
            return "editBooking";
        }).orElse("redirect:/");
    }

    // Aggiorna una prenotazione esistente
    @PostMapping("/bookings/upd")
    public String update(@ModelAttribute("booking") Booking booking, Model model) {
        try {
            validateBookingDates(booking, booking.getId());
            bookingRepository.save(booking);
            model.addAttribute("successMessage", "Prenotazione aggiornata con successo!");
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("booking", booking);
            model.addAttribute("roomNumber", booking.getRoom().getRoomNumber() + " - " + booking.getRoom().getType());
            model.addAttribute("guestName", booking.getGuest().getLastName() + " " + booking.getGuest().getFirstName());
            return "editBooking";
        }
    }

    // Elimina una prenotazione
    @GetMapping("/bookings/delete/{id}")
    public String delete(@PathVariable("id") int id, Model model) {
        try {
            if (bookingRepository.existsById(id)) {
                bookingRepository.deleteById(id);
                model.addAttribute("successMessage", "Prenotazione eliminata con successo!");
            }
            return "redirect:/";
        } catch (DataIntegrityViolationException e) {
            if (e.getMessage() != null && e.getMessage().contains("fk_Payments_Bookings1")) {
                return "error/bookingDeleteError";
            }
            throw e;
        }
    }

    // Lista delle prenotazioni di un determinato ospite
    @GetMapping("/bookings/guest/{guestId}")
    public String listBookingsByGuest(@PathVariable("guestId") int guestId, Model model) {
        Guest guest = guestRepository.findById(guestId)
            .orElseThrow(() -> new IllegalArgumentException("ID ospite non valido: " + guestId));
        List<Booking> bookings = bookingRepository.findByGuest(guest);
        model.addAttribute("bookings", bookings);
        model.addAttribute("guestName", guest.getLastName() + " " + guest.getFirstName());
        return "guestBookings";
    }

    // Calcola il prezzo della prenotazione (AJAX)
    @GetMapping("/bookings/calculate")
    @ResponseBody
    public ResponseEntity<?> calculateBookingPrice(
            @RequestParam int roomId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkIn,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkOut) {
        try {
            Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Stanza non trovata: " + roomId));

            long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
            if (nights <= 0) throw new IllegalArgumentException("La data di check-out deve essere successiva al check-in");

            BigDecimal totalPrice = room.getPrice().multiply(BigDecimal.valueOf(nights));

            Map<String, Object> response = Map.of(
                "totalAmount", totalPrice,
                "nights", nights,
                "pricePerNight", room.getPrice()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = Map.of("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // Validazione date della prenotazione (check-in < check-out e nessun conflitto)
    private void validateBookingDates(Booking booking, int excludeBookingId) {
        if (booking.getCheckOut().isBefore(booking.getCheckIn())) {
            throw new IllegalArgumentException("La data di check-out deve essere successiva al check-in");
        }

        if (bookingRepository.existsOverlappingBooking(
                booking.getRoom().getId(),
                booking.getCheckIn(),
                booking.getCheckOut(),
                excludeBookingId)) {
            throw new IllegalArgumentException("La stanza è già prenotata per le date selezionate");
        }
    }

    // Mostra tutte le prenotazioni (per amministratori)
    @GetMapping("/bookings/all")
    public String getAllBookings(Model model) {
        model.addAttribute("bookings", bookingRepository.findAll());
        return "bookings";
    }

    // Mostra i dettagli di una prenotazione con stato pagamento
    @GetMapping("/bookings/details/{id}")
    public String getBookingDetails(@PathVariable("id") int id, 
                                    @RequestParam(required = false) boolean newBooking,
                                    Model model) {
        Booking booking = bookingRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("ID prenotazione non valido: " + id));

        List<Payment> payments = paymentRepository.findByBooking(booking);
        String paymentStatus = calculatePaymentStatus(booking, payments);

        model.addAttribute("booking", booking);
        model.addAttribute("payments", payments);
        model.addAttribute("paymentStatus", paymentStatus);
        model.addAttribute("newBooking", newBooking);
        return "bookingDetails";
    }

    // Calcola lo stato del pagamento per una prenotazione
    private String calculatePaymentStatus(Booking booking, List<Payment> payments) {
        if (payments == null || payments.isEmpty()) return "Unpaid";

        BigDecimal totalPaid = payments.stream()
            .filter(p -> "Paid".equals(p.getStatus()))
            .map(Payment::getAmountPaid)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return (totalPaid.compareTo(booking.getTotalAmount()) >= 0) ? "Paid" :
               (totalPaid.compareTo(BigDecimal.ZERO) > 0) ? "Partial" : "Unpaid";
    }
}
