package repository;

import entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    // 1. Lấy tất cả công việc của 1 nhân viên
    @Query("SELECT t FROM Task t WHERE t.employee.id = :employeeId")
    List<Task> findByEmployeeId(@Param("employeeId") Long employeeId);

    // 2. Lấy danh sách công việc theo trạng thái hoàn thành (completed = true/false)
    @Query("SELECT t FROM Task t WHERE t.employee.id = :employeeId AND t.completed = :completed")
    List<Task> findByEmployeeIdAndCompleted(@Param("employeeId") Long employeeId, @Param("completed") boolean completed);

    // 3. Lấy công việc được giao trong khoảng thời gian (phục vụ tính KPI tháng)
    @Query("SELECT t FROM Task t WHERE t.employee.id = :employeeId AND t.assignedDate BETWEEN :startDate AND :endDate")
    List<Task> findByEmployeeIdAndAssignedDateBetween(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // 4. Đếm tổng số task của nhân viên trong tháng
    @Query("SELECT COUNT(t) FROM Task t WHERE t.employee.id = :employeeId AND t.assignedDate BETWEEN :startDate AND :endDate")
    long countByEmployeeIdAndAssignedDateBetween(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // 5. Đếm số task đã hoàn thành của nhân viên trong tháng
    @Query("SELECT COUNT(t) FROM Task t WHERE t.employee.id = :employeeId AND t.completed = :completed AND t.assignedDate BETWEEN :startDate AND :endDate")
    long countByEmployeeIdAndCompletedAndAssignedDateBetween(
            @Param("employeeId") Long employeeId,
            @Param("completed") boolean completed,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}