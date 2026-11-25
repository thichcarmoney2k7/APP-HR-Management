package config;

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

            // Chỉ tạo nếu trong DB chưa có tài khoản này
            if (userRepository.findByUsername(adminEmail).isEmpty()) {
                User adminUser = new User();
                adminUser.setUsername(adminEmail);
                // Lưu ý: Nếu AuthService của bạn có dùng BCryptPasswordEncoder thì bọc passwordEncoder.encode("123456")
                adminUser.setPassword("123456");
                adminUser.setUsername("Quản Lý Hệ Thống");
                adminUser.setRole(Role.MANAGER); // Hoặc Role.MANAGER tùy theo enum Role của bạn
                User savedUser = userRepository.save(adminUser);

                // Tạo kèm hồ sơ Employee để quản lý có employeeId
                Employee managerProfile = new Employee();
                managerProfile.setUser(savedUser); // Sửa thành setUserId(savedUser.getId()) nếu entity dùng Long userId
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