package repository;

import entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    // 1. Tìm bản ghi chấm công của nhân viên trong một ngày cụ thể (Dùng để kiểm tra hôm nay đã check-in chưa)
    Optional<Attendance> findByEmployeeIdAndWorkDate(Long employeeId, LocalDate workDate);

    // 2. Lấy danh sách chấm công của 1 nhân viên (sắp xếp ngày mới nhất lên trên cùng)
    List<Attendance> findByEmployeeIdOrderByWorkDateDesc(Long employeeId);

    // 3. Lấy danh sách chấm công của 1 nhân viên từ ngày A đến ngày B (Dùng cho tính thưởng KPI / Lương tháng)
    List<Attendance> findByEmployeeIdAndWorkDateBetween(Long employeeId, LocalDate startDate, LocalDate endDate);

    // 4. Kiểm tra nhân viên đã chấm công ngày hôm nay chưa
    boolean existsByEmployeeIdAndWorkDate(Long employeeId, LocalDate workDate);
}