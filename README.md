# MedVitals HMS - Spring Boot Java Backend Deployment Guide

This backend is built with Spring Boot, Java 17, and JPA Hibernate connecting to MySQL.

---

## 🚀 How to Run on Any System

### Prerequisites
1. **Java Development Kit (JDK)**: JDK 17 or higher.
2. **MySQL Database**: A running MySQL instance (default port 3306).

### Step 1: Configure Database Credentials
Open [application.properties](file:///C:/Users/rahul/.gemini/antigravity/scratch/hospital-consultation-app/backend/src/main/resources/application.properties) and update the datasource connection settings to match the target database configurations:
```properties
spring.datasource.username=YOUR_MYSQL_USERNAME
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

### Step 2: Initialize Database Schemas
Examine the database script [schema.sql](file:///C:/Users/rahul/.gemini/antigravity/scratch/hospital-consultation-app/backend/src/main/resources/schema.sql) and run it inside your MySQL client or IDE to set up the tables, constraints, and spatial indexes.

### Step 3: Run the Application
In your terminal, navigate to this folder and use Maven to run:
```bash
./mvnw spring-boot:run
```
Or open the folder inside any Java IDE (e.g. IntelliJ IDEA, Eclipse, VS Code) and run the main entry point [ConsultationApplication.java](file:///C:/Users/rahul/.gemini/antigravity/scratch/hospital-consultation-app/backend/src/main/java/com/hospital/consultation/ConsultationApplication.java).
