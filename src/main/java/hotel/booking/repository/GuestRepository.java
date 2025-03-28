package hotel.booking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;

import hotel.booking.model.Guest;

public interface GuestRepository extends JpaRepository<Guest, Integer> {

    @NativeQuery("SELECT * FROM Guests WHERE Guests.firstName LIKE %?1% OR Guests.lastName LIKE %?1%")
    List<Guest>findByPatternLike(String pattern);

}
