package server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {"entity"})
@EnableJpaRepositories(basePackages = {"repository"})
@ComponentScan(basePackages = {"server", "controller", "service", "repository", "entity"})
public class HrmServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(HrmServerApplication.class, args);
    }
}