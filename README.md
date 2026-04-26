# room-reservation-service
A  management corporation Service for Room Reservation.

# Room Reservation Service
## Overview
This microservice manages hotel room reservations using event-driven architecture.

## Features
- Confirm reservation via REST API
- Payment handling:
  - Cash → instant confirmation
  - Credit card → external API validation
  - Bank transfer → async confirmation via Kafka
- Auto cancellation if unpaid before 2 days
- Kafka consumer for payment updates

## Tech Stack
- Java 17
- Spring Boot
- Spring Kafka
- H2 Database
- Maven

## Setup
### 1. Clone repo
git clone https://github.com/your-username/room-reservation-service.git

### 2. Build project
mvn clean install

### 3. Run app
mvn spring-boot:run

### 4. Start Kafka
Ensure Kafka is running on localhost:9092

## API
### POST /reservations/confirm

Request:
{
  "customerName": "John",
  "roomNumber": "101",
  "startDate": "2026-05-01",
  "endDate": "2026-05-05",
  "segment": "MEDIUM",
  "paymentMode": "CREDIT_CARD",
  "paymentReference": "DL123456789",
  "amount": 5000
}

Response:
{
  "reservationId": "abc123",
  "status": "CONFIRMED"
}

## Kafka Topic
bank-transfer-payment-update


## Dependent Service: Credit Card Payment Service
This service depends on an external microservice: credit-card-payment-service

## Start credit-card-payment-service
It is required when using:
Payment Mode = CREDIT_CARD

  Example message:
  1401541457 P4145478

## Scheduler
Runs daily at 1 AM to cancel unpaid reservations.


## Future Improvement:
- Replace mock credit-card service with real payment gateway integration
- Add service discovery (Eureka)
- Add API Gateway


### Swagger documentation access : http://localhost:8081/swagger-ui/index.html


##  Sample API Requests

Below are example requests to test different reservation scenarios.

---

###  1. CASH Payment (Immediate Confirmation) 
**Behavior:**
    * Reservation is **confirmed immediately**
    * No external payment validation required

**Request:**
```json
{
  "customerName": "John Doe",
  "roomNumber": "101",
  "startDate": "2026-05-06",
  "endDate": "2026-05-09",
  "segment": "MEDIUM",
  "paymentMode": "CASH",
  "amount": 5000
}
```

---

###  2. CREDIT CARD (Success Case)

 **Behavior:**

* Calls **credit-card-payment-service**
* If payment is **CONFIRMED → reservation confirmed**
* If **REJECTED → error thrown**

**Request:**

```json
{
  "customerName": "Alice Smith",
  "roomNumber": "201",
  "startDate": "2026-06-10",
  "endDate": "2026-06-15",
  "segment": "LARGE",
  "paymentMode": "CREDIT_CARD",
  "paymentReference": "DL123456789",
  "amount": 12000
}
```

---

###  3. BANK TRANSFER (Pending Payment)

 **Behavior:**

* Reservation created with **PENDING_PAYMENT**
* Payment confirmation will come via **Kafka event**
* Reservation will be confirmed after payment event

**Request:**

```json
{
  "customerName": "Raj Kumar",
  "roomNumber": "412",
  "startDate": "2026-04-15",
  "endDate": "2026-04-20",
  "segment": "SMALL",
  "paymentMode": "BANK_TRANSFER",
  "amount": 3000
}
```

---

###  4. Invalid Date Range (> 30 Days)

 **Behavior:**

* Validation fails
* Returns **400 BAD REQUEST**

**Request:**

```json
{
  "customerName": "Long Stay User",
  "roomNumber": "505",
  "startDate": "2026-09-01",
  "endDate": "2026-10-15",
  "segment": "EXTRA_LARGE",
  "paymentMode": "CASH",
  "amount": 50000
}
```

---

##  Notes

* Maximum reservation duration allowed: **30 days**
* Room cannot be double-booked for overlapping dates
* Payment reference is required for:
  * CREDIT_CARD
  * BANK_TRANSFER
* Invalid enum values will return **400 BAD REQUEST**
* All errors follow a standard response format:
  ```json
  {
    "message": "Error description",
    "status": 400,
    "timestamp": "2026-04-15T19:10:00"
  }
  ```

