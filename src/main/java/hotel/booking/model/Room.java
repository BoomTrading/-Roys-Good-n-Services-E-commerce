package hotel.booking.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "Rooms")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Column(name = "roomNumber")
    private String roomNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private RoomType type;
    
    @Column(name = "price")
    private BigDecimal price;
    
    @Column(name = "is_available")
    private boolean is_available;
    
    @Column(name = "description")
    private String description; // Campo aggiunto per la descrizione
    
    @Column(name = "imageUrl")
    private String imageUrl; // Campo aggiunto per l'immagine
    
    // Getter e Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public RoomType getType() { return type; }
    public void setType(RoomType type) { this.type = type; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public boolean isIs_available() { return is_available; }
    public void setIs_available(boolean isAvailable) { this.is_available = isAvailable; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public enum RoomType {
        Single, Double, Suite, Triple, Deluxe, Penthouse
    }
}