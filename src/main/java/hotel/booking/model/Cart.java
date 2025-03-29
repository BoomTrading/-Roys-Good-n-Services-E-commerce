package hotel.booking.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Cart")
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    
    @ManyToOne
    @JoinColumn(name = "Guests_id")
    private Guest guest;
    
    @ManyToOne
    @JoinColumn(name = "Product_id", nullable = true)
    private Product product;
    
    @ManyToOne
    @JoinColumn(name = "Service_id", nullable = true)
    private Service service;
    
    @Column(name = "quantity")
    private int quantity;
    
    @Column(name = "added_at")
    private LocalDateTime addedAt;
    
    // Getter e Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Guest getGuest() { return guest; }
    public void setGuest(Guest guest) { this.guest = guest; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public Service getService() { return service; }
    public void setService(Service service) { this.service = service; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }
}