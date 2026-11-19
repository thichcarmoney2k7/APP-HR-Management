package controller;

import entity.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.EmployeeService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "*")
public class EmployeeController {

    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    // 1. Tuyển dụng ứng viên -> Tạo hồ sơ Employee & nâng quyền User
    @PostMapping("/hire/{candidateId}")
    public ResponseEntity<?> hireCandidateToEmployee(
            @PathVariable Long candidateId,
            @RequestParam(required = false) Double baseSalary,
            @RequestParam(required = false) Integer departmentId,
            @RequestParam(required = false) Long managerId) {
        try {
            Employee employee = employeeService.hireCandidateToEmployee(candidateId, baseSalary, departmentId, managerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(employee);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // 2. Lấy danh sách toàn bộ nhân viên
    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees() {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    // 3. Lấy chi tiết nhân viên theo Employee ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getEmployeeById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(employeeService.getEmployeeById(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // 4. Tìm nhân viên theo User ID (dùng khi đăng nhập để lấy thông tin nhân viên)
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getEmployeeByUserId(@PathVariable Long userId) {
        return employeeService.getEmployeeByUserId(userId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Không tìm thấy nhân viên gắn với User ID: " + userId)));
    }

    // 5. Tìm nhân viên theo Face ID
    @GetMapping("/face")
    public ResponseEntity<?> getEmployeeByFaceId(@RequestParam String faceId) {
        return employeeService.getEmployeeByFaceId(faceId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Không tìm thấy nhân viên có Face ID này")));
    }

    // 6. Tìm nhân viên theo Candidate ID gốc
    @GetMapping("/candidate/{candidateId}")
    public ResponseEntity<?> getEmployeeByCandidateId(@PathVariable Long candidateId) {
        return employeeService.getEmployeeByCandidateId(candidateId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Ứng viên chưa có hồ sơ nhân viên")));
    }

    // 7. Lấy danh sách nhân viên thuộc quyền quản lý của 1 Manager
    @GetMapping("/manager/{managerId}")
    public ResponseEntity<List<Employee>> getEmployeesByManagerId(@PathVariable Long managerId) {
        return ResponseEntity.ok(employeeService.getEmployeesByManagerId(managerId));
    }

    // 8. Tìm nhân viên theo Email
    @GetMapping("/email")
    public ResponseEntity<?> getEmployeeByEmail(@RequestParam String email) {
        return employeeService.getEmployeeByEmail(email)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Không tìm thấy nhân viên với email: " + email)));
    }

    // 9. Đăng ký khuôn mặt mẫu từ ảnh Base64 Webcam JavaFX
    @PostMapping("/{id}/register-face")
    public ResponseEntity<?> registerFaceId(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        try {
            String imageBase64 = payload.getOrDefault("imageBase64", payload.get("faceId"));
            Employee updated = employeeService.registerFaceId(id, imageBase64);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // 10. Cập nhật thông tin hồ sơ nhân viên
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEmployee(@PathVariable Long id, @RequestBody Employee updatedInfo) {
        try {
            return ResponseEntity.ok(employeeService.updateEmployee(id, updatedInfo));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // 11. Xóa nhân viên
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEmployee(@PathVariable Long id) {
        boolean deleted = employeeService.deleteEmployee(id);
        if (deleted) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Đã xóa nhân viên thành công"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "message", "Không tìm thấy nhân viên có ID: " + id));
    }
}