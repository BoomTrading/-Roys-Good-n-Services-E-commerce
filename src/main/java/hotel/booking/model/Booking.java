package hotel.booking.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;


@Entity
@Table(name = "Bookings")
public class Booking {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	@Column(name = "check_in")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate checkIn;

	@Column(name = "check_out")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate checkOut;

	@Column(name = "total_amount")
	private BigDecimal totalAmount;

	@ManyToOne
	@JoinColumn(name = "Guests_id")
	private Guest guest;

	@ManyToOne
	@JoinColumn(name = "Rooms_id")
	private Room room;

	@Column(name = "checkInTime", length = 100)
	private String checkInTime;

	@Column(name = "checkOutTime", length = 100)
	private String checkOutTime;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public LocalDate getCheckIn() {
		return checkIn;
	}

	public void setCheckIn(LocalDate checkIn) {
		this.checkIn = checkIn;
	}

	public LocalDate getCheckOut() {
		return checkOut;
	}

	public void setCheckOut(LocalDate checkOut) {
		this.checkOut = checkOut;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	public Guest getGuest() {
		return guest;
	}

	public void setGuest(Guest guest) {
		this.guest = guest;
	}

	public Room getRoom() {
		return room;
	}

	public void setRoom(Room room) {
		this.room = room;
	}

	public String getCheckInTime() {
		return checkInTime;
	}

	public void setCheckInTime(String checkInTime) {
		this.checkInTime = checkInTime;
	}

	public String getCheckOutTime() {
		return checkOutTime;
	}

	public void setCheckOutTime(String checkOutTime) {
		this.checkOutTime = checkOutTime;
	}
}
