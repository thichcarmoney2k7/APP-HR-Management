package repository;

import entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByEmployeeId(Long employeeId);

    // 2. Lấy danh sách công việc theo trạng thái hoàn thành (completed = true/false)
    List<Task> findByEmployeeIdAndCompleted(Long employeeId, boolean completed);

    // 3. Lấy công việc được giao trong khoảng thời gian (phục vụ tính KPI tháng)
    List<Task> findByEmployeeIdAndAssignedDateBetween(Long employeeId, LocalDate startDate, LocalDate endDate);

    // 4. Đếm tổng số task của nhân viên trong tháng
    long countByEmployeeIdAndAssignedDateBetween(Long employeeId, LocalDate startDate, LocalDate endDate);

    // 5. Đếm số task đã hoàn thành của nhân viên trong tháng
    long countByEmployeeIdAndCompletedAndAssignedDateBetween(Long employeeId, boolean completed, LocalDate startDate, LocalDate endDate);
}