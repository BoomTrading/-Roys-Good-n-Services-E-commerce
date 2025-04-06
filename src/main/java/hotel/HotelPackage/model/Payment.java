package hotel.HotelPackage.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    
    @Column(name = "amount_paid")
    private BigDecimal amountPaid;
    
    @Column(name = "payment_date")
    private LocalDateTime paymentDate;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "payment_method")
    private String paymentMethod;
    
    @Column(name = "transaction_id")
    private String transactionId;
    
    @ManyToOne
    @JoinColumn(name = "Bookings_id", nullable = true)
    private Booking booking;
    
    @ManyToOne
    @JoinColumn(name = "Orders_id", nullable = true)
    private Order order;
    
    // Getter e Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }
    public LocalDateTime getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDateTime paymentDate) { this.paymentDate = paymentDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
}