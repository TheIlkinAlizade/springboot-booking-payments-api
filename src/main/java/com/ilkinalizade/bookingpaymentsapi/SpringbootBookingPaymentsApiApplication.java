package com.ilkinalizade.bookingpaymentsapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SpringbootBookingPaymentsApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootBookingPaymentsApiApplication.class, args);
    }

}
