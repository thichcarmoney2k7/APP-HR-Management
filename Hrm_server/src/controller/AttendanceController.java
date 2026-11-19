//Các cổng REST API tiếp nhận yêu cầu từ Client (tất cả controller)
//API: /api/v1/attendance/check-in

package controller;

import dto.request.FaceCheckinRequest;
import dto.response.AttendanceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.AttendanceService;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "*")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @Autowired
    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    // 1. Điểm danh vào ca bằng khuôn mặt (Face ID)
    @PostMapping("/checkin")
    public ResponseEntity<?> checkIn(@RequestBody FaceCheckinRequest request) {
        try {
            AttendanceResponse response = attendanceService.checkIn(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Lỗi điểm danh vào ca: " + e.getMessage()));
        }
    }

    // 2. Chấm công ra về (Hỗ trợ nhận employeeId qua query param hoặc từ JSON FaceCheckinRequest)
    @PostMapping("/checkout")
    public ResponseEntity<?> checkOut(
            @RequestParam(required = false) Long employeeId,
            @RequestBody(required = false) FaceCheckinRequest request) {
        try {
            Long targetEmpId = (employeeId != null)
                    ? employeeId
                    : (request != null ? request.getEmployeeId() : null);

            if (targetEmpId == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("success", false, "message", "Thiếu thông tin employeeId để check-out!"));
            }

            AttendanceResponse response = attendanceService.checkOut(targetEmpId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Lỗi chấm công ra về: " + e.getMessage()));
        }
    }

    // 3. Kiểm tra nhanh hôm nay nhân viên đã chấm công chưa
    @GetMapping("/employee/{id}/has-checked-in")
    public ResponseEntity<?> hasCheckedInToday(@PathVariable Long id) {
        try {
            boolean checkedIn = attendanceService.hasCheckedInToday(id);
            return ResponseEntity.ok(Map.of("employeeId", id, "hasCheckedIn", checkedIn));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // 4. Lấy chi tiết bản ghi chấm công hôm nay của 1 nhân viên (Hiển thị lên UI JavaFX)
    @GetMapping("/employee/{id}/today")
    public ResponseEntity<?> getTodayAttendance(@PathVariable Long id) {
        try {
            AttendanceResponse response = attendanceService.getTodayAttendance(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // 5. Lấy toàn bộ lịch sử chấm công của 1 nhân viên
    @GetMapping("/employee/{id}")
    public ResponseEntity<?> getEmployeeAttendanceHistory(@PathVariable Long id) {
        try {
            List<AttendanceResponse> history = attendanceService.getEmployeeAttendanceHistory(id);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // 6. Lấy bảng công theo tháng của 1 nhân viên (định dạng monthYear: "yyyy-MM")
    @GetMapping("/employee/{id}/monthly")
    public ResponseEntity<?> getMonthlyAttendance(
            @PathVariable Long id,
            @RequestParam(required = false) String monthYear) {
        try {
            String targetMonth = (monthYear != null && !monthYear.isBlank())
                    ? monthYear.trim()
                    : YearMonth.now().toString();

            List<AttendanceResponse> monthlyList = attendanceService.getMonthlyAttendance(id, targetMonth);
            return ResponseEntity.ok(monthlyList);
        } catch (DateTimeParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Định dạng tháng không hợp lệ (yêu cầu yyyy-MM, ví dụ: 2026-11)"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // 7. Lấy danh sách chấm công toàn bộ nhân viên theo ngày (Dành cho Manager/HR)
    @GetMapping
    public ResponseEntity<?> getAllAttendanceByDate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            LocalDate targetDate = (date != null) ? date : LocalDate.now();
            List<AttendanceResponse> list = attendanceService.getAllAttendanceByDate(targetDate);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}