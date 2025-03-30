package hotel.HotelPackage.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import hotel.HotelPackage.model.Booking;
import hotel.HotelPackage.model.Order;
import hotel.HotelPackage.model.Payment;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    // Trova tutti i pagamenti associati a una prenotazione
    List<Payment> findByBooking(Booking booking);

    // Trova tutti i pagamenti associati a un ordine
    List<Payment> findByOrder(Order order);
}