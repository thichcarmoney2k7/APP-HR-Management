package controller;

import dto.request.TaskAssignRequest;
import dto.response.TaskResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.TaskService;

import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class TaskController {

    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // 1. Manager giao task mới cho nhân viên
    @PostMapping("/assign")
    public ResponseEntity<?> assignTask(@RequestBody TaskAssignRequest request) {
        try {
            TaskResponse response = taskService.assignTask(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Lỗi giao công việc: " + e.getMessage()));
        }
    }

    // 2. Lấy danh sách toàn bộ task trong công ty (Dùng cho Manager/Admin)
    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    // 3, 4, 5. Lấy danh sách task của 1 nhân viên (Hỗ trợ lọc tùy chọn theo trạng thái hoặc tháng)
    @GetMapping("/employee/{id}")
    public ResponseEntity<?> getTasksByEmployee(
            @PathVariable Long id,
            @RequestParam(required = false) Boolean completed,
            @RequestParam(required = false) String monthYear) {
        try {
            if (completed != null) {
                return ResponseEntity.ok(taskService.getTasksByEmployeeAndStatus(id, completed));
            }
            if (monthYear != null && !monthYear.isBlank()) {
                return ResponseEntity.ok(taskService.getTasksByEmployeeAndMonth(id, monthYear.trim()));
            }
            return ResponseEntity.ok(taskService.getTasksByEmployee(id));
        } catch (DateTimeParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Định dạng tháng không hợp lệ (yêu cầu yyyy-MM, ví dụ: 2026-11)"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    //6 manager điều chỉnh trạng thái quyết định đã hoàn thành task hay chx
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateTaskStatus(
            @PathVariable Long id,
            @RequestParam boolean completed) {
        try {
            TaskResponse response = taskService.updateTaskStatus(id, completed);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // 7. Xóa công việc
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id) {
        boolean deleted = taskService.deleteTask(id);
        if (deleted) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã xóa công việc thành công"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "message", "Không tìm thấy công việc có ID: " + id));
    }
}
