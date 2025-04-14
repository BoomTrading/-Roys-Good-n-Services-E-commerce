-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema mydb
-- -----------------------------------------------------
-- -----------------------------------------------------
-- Schema hotel
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema hotel
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `hotel` ;
USE `hotel` ;

-- -----------------------------------------------------
-- Table `hotel`.`Guests`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `hotel`.`Guests` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `firstName` VARCHAR(255) NULL DEFAULT NULL,
  `lastName` VARCHAR(255) NULL DEFAULT NULL,
  `email` VARCHAR(255) NULL DEFAULT NULL,
  `phone` VARCHAR(255) NULL DEFAULT NULL,
  `nationality` VARCHAR(255) NULL DEFAULT NULL,
  `country` VARCHAR(255) NULL DEFAULT NULL,
  `city` VARCHAR(255) NULL DEFAULT NULL,
  `state` VARCHAR(255) NULL DEFAULT NULL,
  `province` VARCHAR(255) NULL DEFAULT NULL,
  `address` VARCHAR(255) NULL DEFAULT NULL,
  `birthDay` DATE NULL DEFAULT NULL,
  `identityCard` VARCHAR(255) NULL DEFAULT NULL,
  `password` VARCHAR(255) NULL DEFAULT NULL,
  `role` VARCHAR(50) NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `unique_email` (`email` ASC) VISIBLE)
ENGINE = InnoDB
AUTO_INCREMENT = 29;


-- -----------------------------------------------------
-- Table `hotel`.`AdmUser`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `hotel`.`AdmUser` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(100) NOT NULL,
  `password` VARCHAR(100) NOT NULL,
  `roles` VARCHAR(100) NULL DEFAULT NULL,
  `name` VARCHAR(100) NULL DEFAULT NULL,
  `guest_id` INT(11) NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `AdmUser_UNIQUE` (`username` ASC) VISIBLE,
  UNIQUE INDEX `unique_username` (`username` ASC) VISIBLE,
  INDEX `fk_admuser_guest` (`guest_id` ASC) VISIBLE,
  CONSTRAINT `fk_admuser_guest`
    FOREIGN KEY (`guest_id`)
    REFERENCES `hotel`.`Guests` (`id`))
ENGINE = InnoDB
AUTO_INCREMENT = 11;


-- -----------------------------------------------------
-- Table `hotel`.`Rooms`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `hotel`.`Rooms` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `roomNumber` VARCHAR(255) NULL DEFAULT NULL,
  `type` ENUM('Single', 'Double', 'Suite', 'Triple', 'Deluxe', 'Penthouse') NULL DEFAULT NULL,
  `price` DECIMAL(38,2) NULL DEFAULT NULL,
  `is_available` TINYINT(1) NULL DEFAULT 1,
  `description` TEXT NULL DEFAULT NULL,
  `image_url` VARCHAR(255) NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `roomNumber_UNIQUE` (`roomNumber` ASC) VISIBLE,
  UNIQUE INDEX `unique_room_number` (`roomNumber` ASC) VISIBLE)
ENGINE = InnoDB
AUTO_INCREMENT = 261;


-- -----------------------------------------------------
-- Table `hotel`.`Bookings`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `hotel`.`Bookings` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `check_in` DATE NULL DEFAULT NULL,
  `check_out` DATE NULL DEFAULT NULL,
  `total_amount` DECIMAL(38,2) NULL DEFAULT NULL,
  `Guests_id` INT(11) NOT NULL,
  `Rooms_id` INT(11) NOT NULL,
  `checkInTime` VARCHAR(100) NULL DEFAULT NULL,
  `checkOutTime` VARCHAR(100) NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_Bookings_Guests_idx` (`Guests_id` ASC) VISIBLE,
  INDEX `fk_Bookings_Rooms1_idx` (`Rooms_id` ASC) VISIBLE,
  CONSTRAINT `fk_Bookings_Guests`
    FOREIGN KEY (`Guests_id`)
    REFERENCES `hotel`.`Guests` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_Bookings_Rooms1`
    FOREIGN KEY (`Rooms_id`)
    REFERENCES `hotel`.`Rooms` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
AUTO_INCREMENT = 38;


-- -----------------------------------------------------
-- Table `hotel`.`Products`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `hotel`.`Products` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `description` TEXT NULL DEFAULT NULL,
  `price` DECIMAL(38,2) NOT NULL,
  `category` VARCHAR(100) NOT NULL,
  `stock` INT(11) NOT NULL,
  `image_url` VARCHAR(500) NULL DEFAULT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB
AUTO_INCREMENT = 18;


-- -----------------------------------------------------
-- Table `hotel`.`Services`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `hotel`.`Services` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(255) NOT NULL,
  `description` TEXT NULL DEFAULT NULL,
  `price` DECIMAL(38,2) NOT NULL,
  `category` VARCHAR(100) NOT NULL,
  `is_available` TINYINT(1) NULL DEFAULT 1,
  `image_url` VARCHAR(500) NULL DEFAULT NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB
AUTO_INCREMENT = 20;


-- -----------------------------------------------------
-- Table `hotel`.`Cart`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `hotel`.`Cart` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `Guests_id` INT(11) NOT NULL,
  `Product_id` INT(11) NULL DEFAULT NULL,
  `Service_id` INT(11) NULL DEFAULT NULL,
  `quantity` INT(11) NOT NULL,
  `added_at` DATETIME NULL DEFAULT CURRENT_TIMESTAMP(),
  `Room_id` INT(11) NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  INDEX `Guests_id` (`Guests_id` ASC) VISIBLE,
  INDEX `Product_id` (`Product_id` ASC) VISIBLE,
  INDEX `Service_id` (`Service_id` ASC) VISIBLE,
  INDEX `fk_cart_room` (`Room_id` ASC) VISIBLE,
  CONSTRAINT `cart_ibfk_1`
    FOREIGN KEY (`Guests_id`)
    REFERENCES `hotel`.`Guests` (`id`),
  CONSTRAINT `cart_ibfk_2`
    FOREIGN KEY (`Product_id`)
    REFERENCES `hotel`.`Products` (`id`),
  CONSTRAINT `cart_ibfk_3`
    FOREIGN KEY (`Service_id`)
    REFERENCES `hotel`.`Services` (`id`),
  CONSTRAINT `fk_cart_room`
    FOREIGN KEY (`Room_id`)
    REFERENCES `hotel`.`Rooms` (`id`))
ENGINE = InnoDB
AUTO_INCREMENT = 17;


-- -----------------------------------------------------
-- Table `hotel`.`Orders`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `hotel`.`Orders` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `Guests_id` INT(11) NOT NULL,
  `order_date` DATETIME NULL DEFAULT CURRENT_TIMESTAMP(),
  `total_amount` DECIMAL(38,2) NOT NULL,
  `status` ENUM('PENDING', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'CANCELLED') NULL DEFAULT 'PENDING',
  PRIMARY KEY (`id`),
  INDEX `Guests_id` (`Guests_id` ASC) VISIBLE,
  CONSTRAINT `orders_ibfk_1`
    FOREIGN KEY (`Guests_id`)
    REFERENCES `hotel`.`Guests` (`id`))
ENGINE = InnoDB
AUTO_INCREMENT = 11;


-- -----------------------------------------------------
-- Table `hotel`.`Order_Items`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `hotel`.`Order_Items` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `Order_id` INT(11) NOT NULL,
  `Product_id` INT(11) NULL DEFAULT NULL,
  `Service_id` INT(11) NULL DEFAULT NULL,
  `quantity` INT(11) NOT NULL,
  `unit_price` DECIMAL(38,2) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `Order_id` (`Order_id` ASC) VISIBLE,
  INDEX `Product_id` (`Product_id` ASC) VISIBLE,
  INDEX `Service_id` (`Service_id` ASC) VISIBLE,
  CONSTRAINT `order_items_ibfk_1`
    FOREIGN KEY (`Order_id`)
    REFERENCES `hotel`.`Orders` (`id`),
  CONSTRAINT `order_items_ibfk_2`
    FOREIGN KEY (`Product_id`)
    REFERENCES `hotel`.`Products` (`id`),
  CONSTRAINT `order_items_ibfk_3`
    FOREIGN KEY (`Service_id`)
    REFERENCES `hotel`.`Services` (`id`))
ENGINE = InnoDB
AUTO_INCREMENT = 11;


-- -----------------------------------------------------
-- Table `hotel`.`payments`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `hotel`.`payments` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `amount_paid` DECIMAL(38,2) NULL DEFAULT NULL,
  `payment_date` DATETIME NULL DEFAULT NULL,
  `status` ENUM('Paid', 'Unpaid', 'Rejected') NULL DEFAULT NULL,
  `Bookings_id` INT(11) NOT NULL,
  `Orders_id` INT(11) NULL DEFAULT NULL,
  `transaction_id` VARCHAR(100) NULL DEFAULT NULL,
  `payment_method` VARCHAR(50) NULL DEFAULT NULL,
  `quantity` INT(11) NULL DEFAULT NULL,
  `notes` VARCHAR(255) NULL DEFAULT NULL,
  `Product_id` INT(11) NULL DEFAULT NULL,
  `Service_id` INT(11) NULL DEFAULT NULL,
  `Room_id` INT(11) NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_Payments_Bookings1_idx` (`Bookings_id` ASC) VISIBLE,
  INDEX `fk_payments_orders` (`Orders_id` ASC) VISIBLE,
  INDEX `fk_product` (`Product_id` ASC) VISIBLE,
  INDEX `fk_service` (`Service_id` ASC) VISIBLE,
  INDEX `fk_room` (`Room_id` ASC) VISIBLE,
  CONSTRAINT `fk_Payments_Bookings1`
    FOREIGN KEY (`Bookings_id`)
    REFERENCES `hotel`.`Bookings` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_payments_orders`
    FOREIGN KEY (`Orders_id`)
    REFERENCES `hotel`.`Orders` (`id`),
  CONSTRAINT `fk_product`
    FOREIGN KEY (`Product_id`)
    REFERENCES `hotel`.`Products` (`id`),
  CONSTRAINT `fk_room`
    FOREIGN KEY (`Room_id`)
    REFERENCES `hotel`.`Rooms` (`id`),
  CONSTRAINT `fk_service`
    FOREIGN KEY (`Service_id`)
    REFERENCES `hotel`.`Services` (`id`))
ENGINE = InnoDB
AUTO_INCREMENT = 31;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
