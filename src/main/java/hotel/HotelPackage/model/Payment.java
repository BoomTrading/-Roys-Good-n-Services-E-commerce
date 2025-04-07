package hotel.HotelPackage.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
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
    
    @ManyToOne
    @JoinColumn(name = "Product_id", nullable = true)
    private Product product;
    
    @ManyToOne
    @JoinColumn(name = "Service_id", nullable = true)
    private Service service;
    
    @ManyToOne
    @JoinColumn(name = "Room_id", nullable = true)
    private Room room;
    
    @Column(name = "quantity")
    private Integer quantity;
    
    @Column(name = "notes")
    private String notes;
    
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
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public Service getService() { return service; }
    public void setService(Service service) { this.service = service; }
    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}