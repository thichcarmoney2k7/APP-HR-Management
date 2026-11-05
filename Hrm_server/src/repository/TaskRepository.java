package repository;

import entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    // Lấy danh sách task của 1 nhân viên
    List<Task> findByEmployeeId(Long employeeId);
}