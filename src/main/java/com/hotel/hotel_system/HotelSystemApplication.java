    package com.hotel.hotel_system;

    import org.springframework.boot.SpringApplication;
    import org.springframework.boot.autoconfigure.SpringBootApplication;
    import org.springframework.scheduling.annotation.EnableScheduling;

    @SpringBootApplication
    @EnableScheduling
    public class HotelSystemApplication {

        public static void main(String[] args) {
            SpringApplication.run(HotelSystemApplication.class, args);
        }

    }
