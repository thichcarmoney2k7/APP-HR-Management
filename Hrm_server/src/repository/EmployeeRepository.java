package repository;

import entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    // Thêm các hàm truy vấn riêng cho AI ở đây nếu cần sau này
}