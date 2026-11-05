//Thao tác SQL tự động (Spring Data JPA)
package repository;

import entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    // Lấy danh sách chấm công của nhân viên trong tháng
    List<Attendance> findByEmployeeIdAndWorkDateBetween(Long employeeId, LocalDate startDate, LocalDate endDate);
}