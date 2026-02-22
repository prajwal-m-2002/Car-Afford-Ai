# 🚗 CarAfford AI – Smart Car Affordability Advisor

https://car-afford-ai.onrender.com/

> **Production-grade fintech web application** built with Java 21, Spring Boot 3, MySQL 8, and Vanilla JS.

![Tech Stack](https://img.shields.io/badge/Java-21-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-green) ![MySQL](https://img.shields.io/badge/MySQL-8.0-blue) ![Architecture](https://img.shields.io/badge/Architecture-Clean-purple)

---

## 🎯 What it Does

**Answers one question clearly:**
> *"Which car can I safely afford based on my salary?"*

It prevents emotional buying by computing:
- ✅ **Safe EMI** – using the 40% disposable income rule
- 🧠 **Financial Stress Score** – proprietary 3-factor engine (0–100)
- 🚗 **Top 3 Car Recommendations** – from a live MySQL catalog
- 📋 **Full Report with BUY / DON'T BUY verdict**

---

## 🛠️ Tech Stack

| Layer       | Technology                            |
|-------------|---------------------------------------|
| Backend     | Java 21, Spring Boot 3.2              |
| ORM         | Spring Data JPA + Hibernate           |
| Validation  | Jakarta Bean Validation               |
| Database    | MySQL 8.0                             |
| Build       | Maven                                 |
| Frontend    | HTML5, CSS3, Vanilla JavaScript (ES6+)|
| API Style   | REST (JSON), Stateless                |
| Utilities   | Lombok, SLF4J                         |

---

## 🏗️ Project Structure

```
carafford-ai/
├── pom.xml
└── src/
    └── main/
        ├── java/com/caraffordai/
        │   ├── CarAffordAiApplication.java      ← Entry point
        │   ├── config/
        │   │   └── WebConfig.java               ← CORS + static resources
        │   ├── controller/
        │   │   ├── UserController.java           ← POST /api/users/register
        │   │   ├── FinanceController.java        ← POST /api/finance/submit
        │   │   ├── EmiController.java            ← POST /api/emi/calculate
        │   │   └── CarController.java            ← GET /api/cars/recommend, /api/report/{id}
        │   ├── service/
        │   │   ├── UserService.java              ← Registration + profile logic
        │   │   ├── EmiCalculationService.java    ← Core EMI formula engine
        │   │   ├── StressScoreService.java       ← Proprietary stress algorithm
        │   │   └── RecommendationService.java    ← Main orchestration pipeline
        │   ├── repository/
        │   │   ├── UserRepository.java
        │   │   ├── ExpenseRepository.java
        │   │   ├── CarRepository.java            ← Custom JPQL recommendation query
        │   │   ├── LoanOptionRepository.java
        │   │   └── AffordabilityReportRepository.java
        │   ├── entity/
        │   │   ├── User.java
        │   │   ├── Expense.java
        │   │   ├── Car.java
        │   │   ├── LoanOption.java
        │   │   └── AffordabilityReport.java
        │   ├── dto/
        │   │   ├── UserRegistrationRequest.java
        │   │   ├── UserResponse.java
        │   │   ├── FinanceSubmitRequest.java
        │   │   ├── EmiCalculationRequest.java
        │   │   ├── EmiCalculationResponse.java
        │   │   ├── CarRecommendationResponse.java
        │   │   └── AffordabilityReportResponse.java
        │   └── exception/
        │       ├── ResourceNotFoundException.java
        │       ├── DuplicateResourceException.java
        │       ├── BusinessValidationException.java
        │       └── GlobalExceptionHandler.java   ← @RestControllerAdvice
        └── resources/
            ├── application.properties
            ├── schema.sql                        ← MySQL DDL + seed data
            └── static/
                ├── index.html                    ← Single-page frontend
                ├── styles.css                    ← Premium dark fintech UI
                └── app.js                        ← Vanilla JS (Fetch API)
```

---

## ⚙️ How to Run

### Prerequisites
- Java 21+ installed
- MySQL 8.0+ running locally
- Maven 3.8+ installed

### Step 1: Database Setup
```sql
-- Run this in MySQL Workbench or CLI:
mysql -u root -p < src/main/resources/schema.sql
```

### Step 2: Configure Database Password
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD_HERE
```

### Step 3: Build and Run
```bash
cd carafford-ai
mvn clean install
mvn spring-boot:run
```

### Step 4: Open the App
```
http://localhost:8080
```

---

## 🔗 API Reference

### 1. Register User
```http
POST /api/users/register
Content-Type: application/json

{
  "name": "Rahul Sharma",
  "email": "rahul@gmail.com",
  "monthlyIncome": 75000
}
```
**Response:** `201 Created`
```json
{
  "id": 1,
  "name": "Rahul Sharma",
  "email": "rahul@gmail.com",
  "monthlyIncome": 75000,
  "message": "Registration successful! Now submit your financial details."
}
```

---

### 2. Submit Financial Profile
```http
POST /api/finance/submit
Content-Type: application/json

{
  "userId": 1,
  "fixedExpenses": 25000,
  "existingEmi": 5000,
  "downPayment": 100000,
  "preferredTenureYears": 5
}
```

---

### 3. Calculate EMI (Standalone)
```http
POST /api/emi/calculate
Content-Type: application/json

{
  "principal": 500000,
  "annualInterestRate": 8.5,
  "tenureYears": 5
}
```
**Response:**
```json
{
  "monthlyEmi": 10264.0,
  "totalPayable": 615840.0,
  "totalInterest": 115840.0,
  "principal": 500000.0,
  "annualInterestRate": 8.5,
  "tenureMonths": 60,
  "tenureYears": 5
}
```

---

### 4. Get Car Recommendations
```http
GET /api/cars/recommend?userId=1
```
Returns the full AffordabilityReport with top 3 cars.

---

### 5. Get Saved Report
```http
GET /api/report/1
```

---

## 🧮 Business Logic Explained

### Safe EMI Rule (40% Rule)
```
Disposable Income = Monthly Income - Fixed Expenses
Safe EMI = Disposable Income × 0.40 - Existing EMIs
```

**Why 40%?** Financial advisors recommend spending no more than 40% of your disposable income on debt. This prevents over-leverage.

---

### EMI Formula (Standard Reducing Balance)
```
EMI = P × r × (1+r)^n / ((1+r)^n - 1)

Where:
  P = Principal (Loan Amount)
  r = Monthly Interest Rate = Annual Rate / 12 / 100
  n = Tenure in Months = Years × 12
```

**Example:**
- P = ₹5,00,000 | Annual Rate = 8.5% | 5 years
- r = 8.5 / 12 / 100 = 0.007083
- n = 60
- **EMI = ₹10,264/month**

---

### Max Loan Amount (Inverted EMI Formula)
```
P = EMI × ((1+r)^n - 1) / (r × (1+r)^n)
```

---

### Financial Stress Score Algorithm
```
Factor 1 (40%): EMI-to-Income Ratio
  → Total EMI / Income | 50% ratio = max stress

Factor 2 (35%): Savings Adequacy
  → Savings left after all payments as % of income
  → Negative savings = max stress

Factor 3 (25%): Existing Debt Burden
  → Existing EMI / Income | 30%+ = high stress

Final Score = weighted average (clamped 0–100)

0–30  = SAFE   ✅
31–60 = CAUTION ⚠️
61–100 = RISKY  ❌
```

---

## 🗄️ Database Schema

```
users ──────────────── expenses (1:1)
  │                    affordability_reports (1:N)
  └──────────────────→ recommended_car_id → cars
                       
loan_options (independent)
cars (independent catalog)
```

**Indexes:**
- `users.email` – Unique, for login lookup
- `cars.price` – For budget range queries
- `affordability_reports.user_id` + `created_at` – For history queries

---

## 🎤 Interview Talking Points

### "Explain your architecture"
> "I used Clean Architecture with 4 layers: Controller (HTTP), Service (business logic), Repository (data access), Entity (domain model). DTOs decouple the API from the database. GlobalExceptionHandler provides centralized error handling."

### "How does the EMI formula work?"
> "It's the standard reducing balance EMI formula: P × r × (1+r)^n / ((1+r)^n - 1). I also inverted it to calculate max loan amount given a safe EMI budget."

### "What is your Stress Score?"
> "It's a proprietary 3-factor weighted metric: EMI-to-income ratio (40% weight), savings adequacy (35%), and existing debt burden (25%). The composite score helps users understand financial risk on a 0–100 scale, far more actionable than a raw EMI number."

### "Why use DTOs?"
> "Three reasons: (1) Decouples API from DB schema, (2) Prevents over-posting attacks, (3) Enables validation annotations without polluting @Entity classes."

### "Why @Transactional?"
> "It wraps database operations in ACID transactions. If any save fails, the entire operation rolls back — no partial data corruption."

### "How did you handle CORS?"
> "Via WebMvcConfigurer's addCorsMappings, which tells browsers our server accepts cross-origin requests from specified origins. In production, we'd replace wildcard with the actual frontend domain."

---

## 🚀 Future Extension Points (Already Architected)

| Feature | Extension Point |
|---------|----------------|
| AI Chat Advisor | `window.openAiChat()` stub in `app.js` |
| Bank Interest Comparison | `LoanOption.bankName` field in DB |
| EV vs Petrol Analysis | `Car.fuelType` filter in `CarRepository` |
| PDF Export | `AffordabilityReport.upgradeAdvice` field ready |
| Authentication | New `config/SecurityConfig.java` + JWT |

---

## 📊 Sample Data Included

**15 Cars:** From ₹3.8L (Datsun redi-GO) to ₹15L (Tata Nexon EV) across Budget, Mid, Premium, and Electric segments.

**9 Loan Options:** SBI, HDFC, ICICI × 3-year, 5-year, 7-year tenures (rates 8.50%–9.25%).

---

*Built with ❤️ as a production-grade fintech showcase. Java 21 | Spring Boot 3.2 | Clean Architecture*
