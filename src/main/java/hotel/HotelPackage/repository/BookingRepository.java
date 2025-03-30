package hotel.HotelPackage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hotel.HotelPackage.model.Booking;
import hotel.HotelPackage.model.Guest;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface BookingRepository extends JpaRepository<Booking, Integer> {

    // Trova tutte le prenotazioni con informazioni su ospite e stanza
    @Query(value = "SELECT b.id, b.check_in, b.check_out, b.total_amount, b.checkinTime, b.checkOuttime, " +
            "g.firstName, g.lastName, r.roomNumber, r.type " +
            "FROM Bookings b " +
            "JOIN Guests g ON b.Guests_id = g.id " +
            "JOIN Rooms r ON b.Rooms_id = r.id " +
            "WHERE b.check_in >= :sdate", nativeQuery = true)
    List<Map<String, Object>> findAllWithGuestRoom(@Param("sdate") String sdate);

    // Verifica se esiste una prenotazione sovrapposta per una stanza
    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
            "WHERE b.room.id = :roomId " +
            "AND b.id != :excludeBookingId " +
            "AND (b.checkIn <= :checkOut AND b.checkOut >= :checkIn)")
    boolean existsOverlappingBooking(
            @Param("roomId") int roomId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("excludeBookingId") int excludeBookingId);

    // Trova tutte le prenotazioni di un ospite
    List<Booking> findByGuest(Guest guest);
}