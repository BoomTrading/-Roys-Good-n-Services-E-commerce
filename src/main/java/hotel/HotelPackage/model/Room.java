package hotel.HotelPackage.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "Rooms")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    
    @Column(name = "roomNumber")
    private String roomNumber;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private RoomType type;
    
    @Column(name = "price")
    private BigDecimal price;
    
    @Column(name = "is_available")
    private boolean isAvailable;
    
    @Column(name = "description") // Descrizione della stanza
    private String description;
    
    @Column(name = "image_url") // URL dell'immagine della stanza
    private String imageUrl;
    
    // Getter e Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public RoomType getType() { return type; }
    public void setType(RoomType type) { this.type = type; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean isAvailable) { this.isAvailable = isAvailable; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public enum RoomType {
        Single, Double, Suite, Triple, Deluxe, Penthouse
    }
}