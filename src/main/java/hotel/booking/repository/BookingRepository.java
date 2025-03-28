package hotel.booking.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hotel.booking.model.Booking;

public interface BookingRepository extends JpaRepository<Booking, Integer> {
    
    @Query(value = "SELECT b.id as booking_id, b.check_in, b.check_out, " +
                   "g.id as guest_id, g.firstName, g.lastName, " + 
                   "r.id as room_id, r.roomNumber as room_number, r.type, b.total_amount as price " +
                   "FROM bookings b " +
                   "JOIN guests g ON b.guests_id = g.id " +
                   "JOIN rooms r ON b.rooms_id = r.id " +
                   "WHERE b.check_in >= ?1 " +
                   "ORDER BY b.check_in", nativeQuery = true)
    List<Map<String, Object>> findAllWithGuestRoom(String date);

    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
           "WHERE b.room.id = :roomId " +
           "AND b.id != :excludeBookingId " +
           "AND ((b.checkIn <= :checkOut) AND (b.checkOut >= :checkIn))")
    boolean existsOverlappingBooking(@Param("roomId") int roomId,
                                   @Param("checkIn") LocalDate checkIn,
                                   @Param("checkOut") LocalDate checkOut,
                                   @Param("excludeBookingId") int excludeBookingId);
}