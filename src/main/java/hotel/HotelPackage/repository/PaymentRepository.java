package hotel.HotelPackage.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import hotel.HotelPackage.model.Booking;
import hotel.HotelPackage.model.Order;
import hotel.HotelPackage.model.Payment;
import hotel.HotelPackage.model.Guest;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    // Trova tutti i pagamenti associati a una prenotazione
    List<Payment> findByBooking(Booking booking);

    // Trova tutti i pagamenti associati a un ordine
    List<Payment> findByOrder(Order order);

    // Trova tutti i pagamenti associati a un ospite
    List<Payment> findByBookingGuest(Guest guest);

    // Add this method if not already present
    List<Payment> findByOrder_Guest(Guest guest);
}