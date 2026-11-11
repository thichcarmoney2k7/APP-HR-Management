package repository;

import entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    // Thêm các hàm truy vấn riêng cho AI ở đây nếu cần sau này
    // 1. Tìm nhân viên bằng User ID (kết nối phiên làm việc khi đăng nhập thành công)
    Optional<Employee> findByUserId(Long userId);

    // 2. Tìm nhân viên bằng Face ID (dùng cho tính năng Chấm công khuôn mặt)
    Optional<Employee> findByFaceId(String faceId);

    // 3. Tìm nhân viên theo Candidate ID (dùng khi tuyển dụng thành công ứng viên)
    Optional<Employee> findByCandidateId(Long candidateId);

    // 4. Lấy danh sách nhân viên thuộc sự quản lý của 1 Manager
    List<Employee> findByManagerId(Long managerId);

    // 5. Tìm nhân viên theo Email
    Optional<Employee> findByEmail(String email);
}