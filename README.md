# Hotel Reservation System

## Project Overview

Hotel Reservation System is a Spring Boot and PostgreSQL based application that manages hotel rooms and reservations through REST APIs. The system includes room management, reservation handling, availability checking, Redis-based distributed locking, payment mock integration, and reservation state management.

## Features

### Room Management

* Add Room
* View All Rooms
* Update Room Details
* Delete Room

### Reservation Management

* Create Reservation
* View Reservations
* Reservation Status Tracking

### Availability Management

* Room Availability Check
* Reservation Overlap Detection

### Concurrency Handling

* Redis Integration
* Redisson Distributed Locking

### Payment Processing

* Payment Mock API

### Reservation State Machine

* HELD
* PAYMENT_PROCESSING
* CONFIRMED
* FAILED
* RELEASED

### CI/CD

* GitHub Actions Pipeline

## Technologies Used

### Backend

* Java
* Spring Boot
* Spring Data JPA
* PostgreSQL
* Redis
* Redisson
* Maven

### Tools

* Git
* GitHub
* GitHub Actions

## API Endpoints

### Room APIs

* GET /rooms
* POST /rooms
* PUT /rooms/{id}
* DELETE /rooms/{id}

### Reservation APIs

* GET /reservations
* POST /reservations

### Availability APIs

* GET /availability

### Payment APIs

* POST /payment/process

## Database

PostgreSQL is used as the primary database for storing room and reservation information.

## Project Status

### Completed

* PostgreSQL Integration
* Room CRUD Operations
* Reservation Module
* Availability Query API
* Redis Integration
* Redisson Distributed Locking
* Payment Mock API
* Reservation State Machine
* GitHub Actions CI Pipeline

### In Progress

* Frontend and Backend Integration

## Author

Mansi Gupta
