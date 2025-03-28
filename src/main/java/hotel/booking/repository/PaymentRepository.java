package hotel.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import hotel.booking.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
}