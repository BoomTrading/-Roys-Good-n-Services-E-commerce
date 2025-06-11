# 🏨 Roys Luxury Hotel System & E-commerce Platform

**Rytami Luxury Experiences** - A comprehensive luxury hotel management and e-commerce platform built with Spring Boot, offering world-class hospitality services and premium products.

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![MariaDB](https://img.shields.io/badge/Database-MariaDB-blue.svg)](https://mariadb.org/)
[![Thymeleaf](https://img.shields.io/badge/Template-Thymeleaf-green.svg)](https://www.thymeleaf.org/)
[![Bootstrap](https://img.shields.io/badge/Frontend-Bootstrap%205.3.3-purple.svg)](https://getbootstrap.com/)

## 🌟 Overview

Roys Luxury Hotel System is a full-featured luxury hospitality and e-commerce platform that provides an all-in-one solution for managing high-end hotel operations, luxury services, and premium product sales. The system caters to affluent clientele seeking exclusive experiences and rare collections.

### 🎯 Key Features

- **🏨 Hotel Management**: Complete booking system with room management
- **🛍️ E-commerce Platform**: Luxury products and rare collections marketplace
- **✈️ Premium Services**: Private jets, yachts, helicopter charters, spa treatments
- **🔐 Role-based Security**: Admin and guest user management
- **💳 Payment Processing**: Comprehensive payment tracking and management
- **🛒 Shopping Cart**: Full cart and order management system
- **📊 Analytics**: Booking analytics and financial reporting

## 🚀 Technology Stack

### Backend
- **Java 21** - Modern Java LTS version
- **Spring Boot 3.4.4** - Main application framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Database operations
- **Hibernate** - ORM framework
- **Spring Validation** - Data validation

### Frontend
- **Thymeleaf** - Server-side templating engine
- **Bootstrap 5.3.3** - Responsive UI framework
- **Font Awesome 6.0** - Icon library
- **SF Pro Display Font** - Apple-inspired typography

### Database
- **MariaDB** - Primary database
- **Flyway** - Database migration (via db/migration folder)

### Build & Development
- **Maven** - Dependency management and build tool
- **Spring Boot DevTools** - Development productivity tools

## 🏗️ System Architecture

### Core Modules

#### 1. 🏨 Hotel Management
- **Room Management**: Different room types (Standard, Deluxe, Suite, Presidential)
- **Booking System**: Check-in/check-out with date validation
- **Guest Management**: Comprehensive guest profiles
- **Payment Tracking**: Booking payment management

#### 2. 🛍️ E-commerce Platform
- **Product Catalog**: Luxury goods and rare collections
- **Shopping Cart**: Multi-item cart management
- **Order Processing**: Complete order lifecycle
- **Inventory Management**: Stock tracking and availability

#### 3. ✨ Luxury Services
- **Spa Services**: Premium wellness treatments
- **Private Transportation**: Jets, helicopters, yachts
- **Golf Services**: Championship golf experiences
- **Concierge Services**: White-glove customer service
- **Real Estate**: Luxury property investments
- **Art & Collections**: Rare and exclusive items

#### 4. 👥 User Management
- **Admin Panel**: Complete system administration
- **Guest Portal**: Customer self-service
- **Role-based Access**: Granular permissions
- **Authentication**: Secure login system

## 📁 Project Structure

```
src/
├── main/
│   ├── java/hotel/HotelPackage/
│   │   ├── BookingApplication.java          # Main Spring Boot application
│   │   ├── controller/                      # REST controllers
│   │   │   ├── BookingController.java       # Hotel booking management
│   │   │   ├── RoomController.java          # Room operations
│   │   │   ├── GuestController.java         # Guest management
│   │   │   ├── ServiceController.java       # Luxury services
│   │   │   ├── ProductController.java       # E-commerce products
│   │   │   ├── CartController.java          # Shopping cart
│   │   │   ├── OrderController.java         # Order processing
│   │   │   └── PaymentController.java       # Payment management
│   │   ├── model/                          # Entity classes
│   │   │   ├── Booking.java                # Hotel bookings
│   │   │   ├── Room.java                   # Hotel rooms
│   │   │   ├── Guest.java                  # Customer profiles
│   │   │   ├── Service.java                # Luxury services
│   │   │   ├── Product.java                # Products
│   │   │   ├── Cart.java                   # Shopping cart items
│   │   │   ├── Order.java                  # Orders
│   │   │   ├── OrderItem.java              # Order line items
│   │   │   └── Payment.java                # Payment records
│   │   └── repository/                     # Data access layer
│   └── resources/
│       ├── application.properties          # Application configuration
│       ├── db/migration/                   # Database migrations
│       ├── static/                         # Static assets (CSS, JS, images)
│       └── templates/                      # Thymeleaf templates
│           ├── index.html                  # Homepage
│           ├── spa.html                    # Spa services
│           ├── privatejet.html             # Private jet services
│           ├── yacht.html                  # Yacht services
│           ├── helicopter.html             # Helicopter services
│           ├── golf.html                   # Golf services
│           ├── luxuryrealestate.html       # Real estate
│           ├── rarecollections.html        # Rare collections
│           └── [other templates...]
└── test/                                   # Test classes
```

## 🛠️ Installation & Setup

### Prerequisites
- **Java 21** or higher
- **Maven 3.6+**
- **MariaDB 10.5+**
- **Git**

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/HotelSystem-E-commerce.git
cd HotelSystem-E-commerce
```

### 2. Database Setup
```sql
-- Create database
CREATE DATABASE hotel;

-- Create user (optional)
CREATE USER 'hotel_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON hotel.* TO 'hotel_user'@'localhost';
FLUSH PRIVILEGES;
```

### 3. Configure Application
Edit `src/main/resources/application.properties`:
```properties
# Database Configuration
spring.datasource.url=jdbc:mariadb://localhost:3306/hotel
spring.datasource.username=root
spring.datasource.password=your_password

# JPA Configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
```

### 4. Build and Run
```bash
# Build the application
mvn clean compile

# Run the application
mvn spring-boot:run

# Or run the JAR file
mvn clean package
java -jar target/booking-0.0.1-SNAPSHOT.jar
```

### 5. Access the Application
- **Application URL**: http://localhost:8080
- **Admin Panel**: Login with admin credentials
- **Guest Portal**: Register as a new guest

## 🎮 Usage Guide

### For Administrators
1. **Dashboard**: Monitor bookings, orders, and payments
2. **Room Management**: Add/edit/delete hotel rooms
3. **Service Management**: Manage luxury services (spa, transportation)
4. **Product Management**: Handle e-commerce inventory
5. **Guest Management**: View and manage guest profiles
6. **Payment Tracking**: Monitor all financial transactions

### For Guests
1. **Browse Services**: Explore luxury offerings
2. **Make Bookings**: Reserve hotel rooms
3. **Shop Products**: Purchase luxury goods
4. **Manage Cart**: Add/remove items
5. **Place Orders**: Complete purchases
6. **View History**: Track bookings and orders

## 🌐 API Endpoints

### Hotel Management
- `GET /` - Homepage with booking overview
- `GET /bookings/all` - All bookings (Admin)
- `POST /bookings/ins` - Create new booking
- `GET /rooms/all` - List all rooms
- `GET /guests/all` - List all guests

### E-commerce
- `GET /products/all` - Product catalog
- `GET /products/art` - Art collections
- `GET /products/rarecollections` - Rare collections
- `GET /cart` - Shopping cart
- `POST /cart/add` - Add to cart
- `GET /orders` - Order history

### Luxury Services
- `GET /services/spa` - Spa treatments
- `GET /services/helicopter` - Helicopter services
- `GET /services/privatejet` - Private jet charters
- `GET /services/yacht` - Yacht rentals
- `GET /services/golf` - Golf services

## 🔒 Security Features

- **Spring Security Integration**
- **Role-based Access Control** (ADMIN/USER)
- **Method-level Security** with `@PreAuthorize`
- **CSRF Protection**
- **Password Encryption**
- **Session Management**

## 🗄️ Database Schema

### Core Tables
- **Guests** - Customer information
- **Rooms** - Hotel room inventory
- **Bookings** - Hotel reservations
- **Products** - E-commerce catalog
- **Services** - Luxury service offerings
- **Cart** - Shopping cart items
- **Orders** - Purchase orders
- **Order_Items** - Order line items
- **Payments** - Payment transactions

## 🎨 UI/UX Features

- **Apple-inspired Design** with SF Pro Display font
- **Responsive Layout** using Bootstrap 5
- **Modern Color Scheme** (#f5f5f7 backgrounds, #0071e3 accents)
- **Intuitive Navigation** with role-based menus
- **Interactive Elements** with smooth animations
- **Mobile-friendly** responsive design

## 🚀 Deployment

### Local Development
```bash
mvn spring-boot:run
```

### Production Deployment
```bash
mvn clean package
java -jar target/booking-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### Docker Deployment (Optional)
```dockerfile
FROM openjdk:21-jre-slim
COPY target/booking-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 🔧 Configuration

### Key Configuration Options
```properties
# Application Name
spring.application.name=booking

# Database Settings
spring.datasource.url=jdbc:mariadb://localhost:3306/hotel
spring.jpa.hibernate.ddl-auto=validate

# Security Settings
spring.security.user.name=admin
spring.security.user.password=admin123

# File Upload Settings
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

For support and questions:
- **Email**: support@rytamiluxury.com
- **Documentation**: Check the wiki section
- **Issues**: Create an issue on GitHub
- **Concierge Service**: Available 24/7 for premium support

## 🏆 Acknowledgments

- **Spring Boot Team** for the excellent framework
- **Bootstrap Team** for the responsive UI components
- **MariaDB** for the reliable database solution
- **Thymeleaf** for the powerful templating engine

---

**© 2025 Roys Luxury Goods n' Services** - *Experience Luxury Redefined*

[![GitHub stars](https://img.shields.io/github/stars/yourusername/HotelSystem-E-commerce.svg)](https://github.com/yourusername/HotelSystem-E-commerce/stargazers)
[![GitHub forks](https://img.shields.io/github/forks/yourusername/HotelSystem-E-commerce.svg)](https://github.com/yourusername/HotelSystem-E-commerce/network)
[![GitHub issues](https://img.shields.io/github/issues/yourusername/HotelSystem-E-commerce.svg)](https://github.com/yourusername/HotelSystem-E-commerce/issues)
