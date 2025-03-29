package hotel.booking.repository;

import hotel.booking.model.Booking;
import hotel.booking.model.Order;
import hotel.booking.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    // Trova tutti i pagamenti associati a una prenotazione
    List<Payment> findByBooking(Booking booking);

    // Trova tutti i pagamenti associati a un ordine
    List<Payment> findByOrder(Order order);
}