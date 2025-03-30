package hotel.HotelPackage.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "Order_Items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    
    @ManyToOne
    @JoinColumn(name = "Order_id")
    private Order order;
    
    @ManyToOne
    @JoinColumn(name = "Product_id", nullable = true)
    private Product product;
    
    @ManyToOne
    @JoinColumn(name = "Service_id", nullable = true)
    private Service service;
    
    @Column(name = "quantity")
    private int quantity;
    
    @Column(name = "unit_price")
    private BigDecimal unitPrice;
    
    // Getter e Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public Service getService() { return service; }
    public void setService(Service service) { this.service = service; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
}