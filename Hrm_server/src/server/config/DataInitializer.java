package server.config;

import entity.Employee;
import entity.User;
import enums.Role; // Nếu dự án bạn dùng String cho role thì đổi thành "ADMIN" hoặc "MANAGER"
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import repository.EmployeeRepository;
import repository.UserRepository;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initDefaultManager(UserRepository userRepository,
                                                EmployeeRepository employeeRepository) {
        return args -> {
            String adminEmail = "admin@hrm.com";

            // Xóa bản ghi bị lưu sai username trước đó (nếu có)
            userRepository.findByUsername("Quản Lý Hệ Thống").ifPresent(wrongUser -> {
                employeeRepository.findAll().stream()
                        .filter(e -> e.getUser() != null && e.getUser().getId().equals(wrongUser.getId()))
                        .forEach(employeeRepository::delete);
                userRepository.delete(wrongUser);
            });

            if (userRepository.findByUsername(adminEmail).isEmpty()) {
                User adminUser = new User();
                adminUser.setUsername(adminEmail);
                adminUser.setPassword("123456"); // Đổi thành passwordEncoder.encode("123456") nếu AuthService dùng BCrypt
                adminUser.setRole(Role.MANAGER);
                User savedUser = userRepository.save(adminUser);

                Employee managerProfile = new Employee();
                managerProfile.setUser(savedUser);
                managerProfile.setFullName("Quản Lý Hệ Thống");
                managerProfile.setEmail(adminEmail);
                managerProfile.setPosition("HR Manager");
                managerProfile.setBaseSalary(25000000.0);
                employeeRepository.save(managerProfile);

                System.out.println(">>> Đã khởi tạo tài khoản Quản lý mặc định: " + adminEmail + " / 123456");
            }
        };
    }
}