//Tính toán tổng hợp KPI nhân viên
package service;

import entity.Employee;
import entity.KPI;
import entity.Task;
import entity.Attendance;
import repository.EmployeeRepository;
import repository.KPIRepository;
import repository.TaskRepository;
import repository.AttendanceRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class KPIService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private KPIRepository kpiRepository;

    @Autowired
    private GeminiService geminiService; // Service gọi Gemini AI API

    public KPI evaluateEmployeeKPI(Long employeeId, String monthYear) {
        // 1. Lấy thông tin nhân viên
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));

        // 2. Xác định ngày bắt đầu và kết thúc tháng (VD: "2026-11" -> 2026-11-01 đến 2026-11-30)
        YearMonth ym = YearMonth.parse(monthYear);
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        // 3. Lấy danh sách Task & Tính tỷ lệ hoàn thành (%)
        List<Task> monthlyTasks = taskRepository.findByEmployeeIdAndAssignedDateBetween(employeeId, startDate, endDate);
        int totalTasks = monthlyTasks.size();
        long completedTasks = monthlyTasks.stream().filter(Task::getCompleted).count();

        double taskCompletionRate = 0.0;
        if (totalTasks > 0) {
            taskCompletionRate = ((double) completedTasks / totalTasks) * 100;
        }

        // 4. Lấy dữ liệu Chấm công trong tháng
        List<Attendance> attendances = attendanceRepository.findByEmployeeIdAndWorkDateBetween(employeeId, startDate, endDate);
        int daysWorked = attendances.size();
        int totalLateMinutes = attendances.stream().mapToInt(Attendance::getLateMinutes).sum();

        // 5. Gom dữ liệu Task + Attendance tạo Prompt cho Gemini AI
        String prompt = String.format("""
            Hãy đánh giá hiệu suất công việc (KPI) và đề xuất tiền thưởng cho nhân viên:
            - Họ tên: %s (Vị trí: %s)
            - Tháng đánh giá: %s
            - Số Task được giao: %d
            - Số Task đã hoàn thành: %d (Tỷ lệ hoàn thành: %.1f%%)
            - Số ngày đi làm: %d ngày
            - Tổng số phút đi trễ: %d phút

            Yêu cầu:
            1. Đánh giá ngắn gọn ý thức làm việc và tiến độ công việc dựa trên tỷ lệ hoàn thành Task.
            2. Gợi ý số tiền thưởng đề xuất (từ 0 đến 2,000,000 VNĐ).
            """,
                employee.getFullName(), employee.getPosition(), monthYear,
                totalTasks, completedTasks, taskCompletionRate,
                daysWorked, totalLateMinutes
        );

        // 6. Gửi dữ liệu sang AI lấy đánh giá
        String aiFeedback = geminiService.callGeminiAPI(prompt);

        // 7. Tạo đối tượng KPI và lưu xuống Database
        KPI kpi = new KPI();
        kpi.setEmployee(employee);
        kpi.setMonthYear(monthYear);
        kpi.setTargetKpi(100.0); // Target là 100%
        kpi.setAchievedKpi(taskCompletionRate); // Đạt được tính theo tỷ lệ hoàn thành Task
        kpi.setAiFeedback(aiFeedback);
        kpi.setBonusAmount(taskCompletionRate >= 80 ? 1000000.0 : 0.0); // Ví dụ thưởng nếu xong >= 80% task

        return kpiRepository.save(kpi);
    }
}