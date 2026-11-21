package controller;

import dto.response.KPISummaryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import service.KPIService;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/kpis")
@CrossOrigin(origins = "*")
public class KPIController {

    private final KPIService kpiService;

    @Autowired
    public KPIController(KPIService kpiService) {
        this.kpiService = kpiService;
    }

    // 1. Đánh giá KPI nhân viên bằng AI & Lưu/Cập nhật Database
    @PostMapping("/evaluate")
    public ResponseEntity<?> evaluateEmployeeKPI(
            @RequestParam Long employeeId,
            @RequestParam(required = false) String monthYear) {
        try {
            String targetMonth = (monthYear != null && !monthYear.isBlank())
                    ? monthYear.trim()
                    : YearMonth.now().toString(); // Mặc định lấy tháng hiện tại dạng "yyyy-MM"

            KPISummaryResponse response = kpiService.evaluateEmployeeKPI(employeeId, targetMonth);
            return ResponseEntity.ok(response);
        } catch (DateTimeParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Định dạng tháng không hợp lệ (yêu cầu yyyy-MM, ví dụ: 2026-11)"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Lỗi đánh giá KPI: " + e.getMessage()));
        }
    }

    // 2. Xem bảng điểm KPI của 1 nhân viên theo tháng (mặc định tháng hiện tại nếu không truyền)
    @GetMapping("/employee/{id}")
    public ResponseEntity<?> getKPIByEmployeeAndMonth(
            @PathVariable Long id,
            @RequestParam(required = false) String monthYear) {
        try {
            String targetMonth = (monthYear != null && !monthYear.isBlank())
                    ? monthYear.trim()
                    : YearMonth.now().toString();

            KPISummaryResponse response = kpiService.getKPIByEmployeeAndMonth(id, targetMonth);
            return ResponseEntity.ok(response);
        } catch (DateTimeParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Định dạng tháng không hợp lệ (yêu cầu yyyy-MM)"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // 3. Xem toàn bộ lịch sử KPI qua các tháng của 1 nhân viên
    @GetMapping("/employee/{id}/history")
    public ResponseEntity<?> getKPIHistoryByEmployee(@PathVariable Long id) {
        try {
            List<KPISummaryResponse> history = kpiService.getKPIHistoryByEmployee(id);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // 4. Lấy báo cáo KPI của toàn công ty trong 1 tháng (Dùng cho Admin/Manager)
    @GetMapping
    public ResponseEntity<?> getCompanyKPIByMonth(@RequestParam(required = false) String monthYear) {
        try {
            String targetMonth = (monthYear != null && !monthYear.isBlank())
                    ? monthYear.trim()
                    : YearMonth.now().toString();

            List<KPISummaryResponse> companyReport = kpiService.getCompanyKPIByMonth(targetMonth);
            return ResponseEntity.ok(companyReport);
        } catch (DateTimeParseException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "Định dạng tháng không hợp lệ (yêu cầu yyyy-MM)"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
