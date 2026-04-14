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

Example message:
1401541457 P4145478

## Scheduler
Runs daily at 1 AM to cancel unpaid reservations.

