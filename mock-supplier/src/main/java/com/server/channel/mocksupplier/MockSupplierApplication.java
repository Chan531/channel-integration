package com.server.channel.mocksupplier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(excludeName = {
        "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration",
        "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
})
public class MockSupplierApplication {

    public static void main(String[] args) {
        SpringApplication.run(MockSupplierApplication.class, args);
    }
}
