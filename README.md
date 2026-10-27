<div align="center">
  <img src="sql/readme/header.png" alt="ARA Motorhub Logo"/>
</div>

---

> **find** your  
> motor **needs.**

---

## Project Overview

ARA Motorhub is a desktop e-commerce and management system for motorcycle parts, built with JavaFX and Maven and backed by a MySQL database. The system provides separate workflows for customers, sellers, and administrators, covering product browsing, ordering, inventory management, store management, and user administration.

>  Created as a final requirement for my Advanced Database Systems class

---

<br>

<p align="center">
  <img src="./sql/readme/ara-logo.gif" alt="ARA Motorhub Animated Logo" width="300"/>
</p>

<br>
<br>

<div align="center">
  <img src="sql/readme/2.png" alt="ARA Motorhub Seller Showcase" width="800"/>
</div>

<br>
<br>

<div align="center">
  <img src="sql/readme/3.png" alt="ARA Motorhub Admin Showcase" width="800"/>
</div>

<br>
<br>
<br>

<div align="center">
  <img src="sql/readme/1.png" alt="ARA Motorhub UI/UX Showcase" width="800"/>
</div>

---

## How to Run

> **Note:** JavaFX and the MySQL driver are pulled in by Maven, no manual setup needed.

### Prerequisites:

- Java JDK 21+
- Apache Maven 3.8+
- MySQL Server / XAMPP

### Steps to Run:

1. **Clone the repository:**

   ```bash
   git clone https://github.com/OG-CZ/aramotorhub.git
   cd aramotorhub
   ```

2. **Setup Database:**

- Create a database named `aramotorhubdb` and import `sql/aramotorhubdb.sql` into it (phpMyAdmin or MySQL Workbench).

3. **Configure Database Connection:**

- Open DatabaseConnection.java under:
  ```bash
  src/main/java/com/ogcz/app/database/DatabaseConnection.java
  ```
- Update with your local database credentials (host, db name, user, password). The defaults match a fresh XAMPP install (`root`, no password).

4. **Run the app using Maven:**

   ```bash
   mvn clean javafx:run
   ```

---

## Database Design

<div align="center">
  <img src="erd-model-aramotorhub.png" alt="ARA Motorhub ERD" width="800"/>
</div>

<br>

<div align="center">
  <img src="er-diagram-chens-model-aramotorhub.jpg" alt="ARA Motorhub ER Diagram (Chen's model)" width="800"/>
</div>

---

## Technologies Used

- Java Programming Language
- JavaFX (UI Framework)
- Maven (Build & Dependency Management)
- FXML (Declarative UI)
- CSS for Styling
- SceneBuilder (for designing UI visually)
- MySQL (Database)
- XAMPP with phpMyAdmin (Local DB Server)

---

## Features

### Admin Side

- Full CRUD operations:
  - Manage Admins, Users, Stores
  - Manage Inventory, Products, Categories
- Stock Management: Available / Out of Stock

### Customer Side

- Step 1 & 2 Registration
- Customer Home Pages
- Buying Process:
  - Search & Filter Products
  - Add to Cart (with Spinner Quantity, capped at available stock)
  - View Seller Shops
  - Checkout (stock is deducted when the order is placed)

### Seller Side

- Search & Filter Own Products
- Sales Dashboard (Informative Analytics)
- Manage Inventory
- Upload & Edit Products
- View Other Products & Shops (for reference only)
- Cannot Purchase Products (seller account restricted from buying)

### User Settings

- Edit Profile Information
- Delete Account
- Log Out

---

<div align="center">
  <img src="sql/readme/header.png" alt="ARA Motorhub Logo"/>
</div>

