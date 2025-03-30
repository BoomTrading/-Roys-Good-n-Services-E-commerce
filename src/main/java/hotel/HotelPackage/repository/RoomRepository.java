package hotel.HotelPackage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import hotel.HotelPackage.model.Room;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Integer> {

    // Cerca stanze in base a un pattern (usato per la ricerca e l'autocompletamento)
    @Query("SELECT r FROM Room r WHERE r.roomNumber LIKE %:pattern% OR r.type LIKE %:pattern%")
    List<Room> findByPatternLike(@Param("pattern") String pattern);

    // Trova stanze disponibili per tipo
    @Query("SELECT r FROM Room r WHERE r.type = :roomType AND r.isAvailable = true")
    List<Room> findAvailableRoomsByType(@Param("roomType") Room.RoomType roomType);

    // Verifica se esiste una stanza con un determinato numero
    boolean existsByRoomNumber(String roomNumber);

    // Verifica se esiste una stanza con un determinato numero, escludendo un ID specifico
    @Query("SELECT COUNT(r) > 0 FROM Room r WHERE r.roomNumber = :roomNumber AND r.id != :id")
    boolean existsByRoomNumberAndIdNot(@Param("roomNumber") String roomNumber, @Param("id") int id);
}