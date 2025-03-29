package hotel.booking.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "Bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    
    @Column(name = "check_in")
    private LocalDate checkIn;
    
    @Column(name = "check_out")
    private LocalDate checkOut;
    
    @Column(name = "total_amount")
    private BigDecimal totalAmount;
    
    @ManyToOne
    @JoinColumn(name = "Guests_id")
    private Guest guest;
    
    @ManyToOne
    @JoinColumn(name = "Rooms_id")
    private Room room;
    
    @Column(name = "checkinTime")
    private String checkinTime;
    
    @Column(name = "checkOuttime")
    private String checkOuttime;
    
    // Getter e Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public LocalDate getCheckIn() { return checkIn; }
    public void setCheckIn(LocalDate checkIn) { this.checkIn = checkIn; }
    public LocalDate getCheckOut() { return checkOut; }
    public void setCheckOut(LocalDate checkOut) { this.checkOut = checkOut; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public Guest getGuest() { return guest; }
    public void setGuest(Guest guest) { this.guest = guest; }
    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }
    public String getCheckinTime() { return checkinTime; }
    public void setCheckinTime(String checkinTime) { this.checkinTime = checkinTime; }
    public String getCheckOuttime() { return checkOuttime; }
    public void setCheckOuttime(String checkOuttime) { this.checkOuttime = checkOuttime; }
}