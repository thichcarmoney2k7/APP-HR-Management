package repository;

import entity.KPI;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KPIRepository extends JpaRepository<KPI, Long> {
    // 1. Tìm bản ghi KPI của nhân viên trong một tháng cụ thể (định dạng "MM-yyyy", vd: "03-2026")
    Optional<KPI> findByEmployeeIdAndMonthYear(Long employeeId, String monthYear);

    // 2. Xem lịch sử KPI của 1 nhân viên (sắp xếp theo tháng mới nhất)
    List<KPI> findByEmployeeIdOrderByMonthYearDesc(Long employeeId);

    // 3. Lấy danh sách KPI của toàn bộ công ty trong 1 tháng (dùng cho Admin xem báo cáo)
    List<KPI> findByMonthYear(String monthYear);
}