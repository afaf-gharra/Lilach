# Lilach System

## Overview
The Lilach System is a Java-based application consisting of a REST API server and a JavaFX client. The server handles user authentication and data management, while the client provides a user-friendly interface for interaction.

## Project Structure
```
lilach-system/
├── LilachServer/ (REST API)
│   ├── src/main/java/com/lilach/server/
│   │   ├── Main.java
│   │   ├── models/
│   │   │   └── User.java
│   │   ├── services/
│   │   │   └── DatabaseService.java
│   │   └── controllers/
│   │       └── AuthController.java
│   ├── src/main/resources/
│   │   └── hibernate.cfg.xml
│   └── pom.xml
│
└── LilachClient/ (JavaFX)
    ├── src/main/java/com/lilach/client/
    │   ├── Main.java
    │   ├── controllers/
    │   │   └── LoginController.java
    │   ├── services/
    │   │   └── ApiService.java
    │   └── models/
    │       └── DTO.java
    ├── src/main/resources/com/lilach/client/
    │   ├── views/
    │   │   └── login.fxml
    │   └── css/
    │       └── styles.css
    └── pom.xml
```

## Getting Started

### Prerequisites
- Java Development Kit (JDK) 11 or higher
- Maven

### Setup Instructions

1. **Clone the Repository**
   ```
   git clone <repository-url>
   cd lilach-system
   ```

2. **Build the Server**
   Navigate to the `LilachServer` directory and run:
   ```
   mvn clean install
   ```

3. **Run the Server**
   Execute the following command to start the server:
   ```
   java -cp target/lilach-server-1.0-SNAPSHOT.jar com.lilach.server.Main
   ```

4. **Build the Client**
   Navigate to the `LilachClient` directory and run:
   ```
   mvn clean install
   ```

5. **Run the Client**
   Execute the following command to start the client:
   ```
   java -cp target/lilach-client-1.0-SNAPSHOT.jar com.lilach.client.Main
   ```

## Usage
- The client application allows users to register and log in.
- Upon successful login, users can access the main features of the application.

## Contributing
Contributions are welcome! Please submit a pull request or open an issue for any enhancements or bug fixes.

## License
This project is licensed under the MIT License. See the LICENSE file for details.


## create jars

cd /LilachClient; mvn clean package
cd /LilachServer; mvn clean package