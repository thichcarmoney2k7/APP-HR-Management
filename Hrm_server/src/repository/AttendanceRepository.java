package repository;

import entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    /// 1. Tìm bản ghi chấm công của nhân viên trong một ngày cụ thể
    @Query("SELECT a FROM Attendance a WHERE a.employee.id = :employeeId AND a.workDate = :workDate")
    Optional<Attendance> findByEmployeeIdAndWorkDate(@Param("employeeId") Long employeeId, @Param("workDate") LocalDate workDate);

    // 2. Lấy danh sách chấm công của 1 nhân viên (sắp xếp ngày mới nhất lên trên cùng)
    @Query("SELECT a FROM Attendance a WHERE a.employee.id = :employeeId ORDER BY a.workDate DESC")
    List<Attendance> findByEmployeeIdOrderByWorkDateDesc(@Param("employeeId") Long employeeId);

    // 3. Lấy danh sách chấm công của 1 nhân viên từ ngày A đến ngày B
    @Query("SELECT a FROM Attendance a WHERE a.employee.id = :employeeId AND a.workDate BETWEEN :startDate AND :endDate")
    List<Attendance> findByEmployeeIdAndWorkDateBetween(@Param("employeeId") Long employeeId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // 4. Kiểm tra nhân viên đã chấm công ngày hôm nay chưa
    @Query("SELECT COUNT(a) > 0 FROM Attendance a WHERE a.employee.id = :employeeId AND a.workDate = :workDate")
    boolean existsByEmployeeIdAndWorkDate(@Param("employeeId") Long employeeId, @Param("workDate") LocalDate workDate);

    // 5. Lấy danh sách chấm công theo ngày
    List<Attendance> findByWorkDate(LocalDate workDate);
}