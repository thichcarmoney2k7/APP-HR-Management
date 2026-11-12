package service;

import dto.response.KPISummaryResponse;
import entity.Attendance;
import entity.Employee;
import entity.KPI;
import entity.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.AttendanceRepository;
import repository.EmployeeRepository;
import repository.KPIRepository;
import repository.TaskRepository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class KPIService {

    private final EmployeeRepository employeeRepository;
    private final TaskRepository taskRepository;
    private final AttendanceRepository attendanceRepository;
    private final KPIRepository kpiRepository;
    private final GeminiService geminiService;

    @Autowired
    public KPIService(EmployeeRepository employeeRepository,
                      TaskRepository taskRepository,
                      AttendanceRepository attendanceRepository,
                      KPIRepository kpiRepository,
                      GeminiService geminiService) {
        this.employeeRepository = employeeRepository;
        this.taskRepository = taskRepository;
        this.attendanceRepository = attendanceRepository;
        this.kpiRepository = kpiRepository;
        this.geminiService = geminiService;
    }

    /**
     - Đánh giá KPI nhân viên bằng AI & Lưu/Cập nhật Database

     */
    public KPISummaryResponse evaluateEmployeeKPI(Long employeeId, String monthYear) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên!"));

        YearMonth ym = YearMonth.parse(monthYear);
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        // Sử dụng đếm trực tiếp từ SQL để tối ưu hiệu năng
        long totalTasks = taskRepository.countByEmployeeIdAndAssignedDateBetween(employeeId, startDate, endDate);
        long completedTasks = taskRepository.countByEmployeeIdAndCompletedAndAssignedDateBetween(employeeId, true, startDate, endDate);

        double taskCompletionRate = totalTasks > 0 ? ((double) completedTasks / totalTasks) * 100 : 0.0;

        List<Attendance> attendances = attendanceRepository.findByEmployeeIdAndWorkDateBetween(employeeId, startDate, endDate);
        int daysWorked = attendances.size();
        int totalLateMinutes = attendances.stream()
                .mapToInt(a -> a.getLateMinutes() != null ? a.getLateMinutes() : 0)
                .sum();

        String prompt = String.format("""
            Hãy đánh giá hiệu suất công việc (KPI) và đề xuất tiền thưởng cho nhân viên:
- Họ tên: %s (Vị trí: %s)

- Tháng đánh giá: %s

- Số Task được giao: %d

- Số Task đã hoàn thành: %d (Tỷ lệ hoàn thành: %.1f%%)

- Số ngày đi làm: %d ngày

- Tổng số phút đi trễ: %d phút


            Yêu cầu:
1. Đánh giá ngắn gọn ý thức làm việc và tiến độ công việc.

            2. Gợi ý số tiền thưởng đề xuất (VNĐ).
            """,
                employee.getFullName(), employee.getPosition(), monthYear,
                totalTasks, completedTasks, taskCompletionRate,
                daysWorked, totalLateMinutes
        );

        String aiFeedback = geminiService.callGeminiAPI(prompt);
        double bonus = (taskCompletionRate >= 80.0 && totalLateMinutes < 30) ? 1000000.0 : 0.0;

        // Tìm bản ghi cũ nếu đã từng đánh giá tháng này thì cập nhật, chưa thì tạo mới
        KPI kpi = kpiRepository.findByEmployeeIdAndMonthYear(employeeId, monthYear)
                .orElse(new KPI());

        kpi.setEmployee(employee);
        kpi.setMonthYear(monthYear);
        kpi.setTargetKpi(100.0);
        kpi.setAchievedKpi(taskCompletionRate);
        kpi.setAiFeedback(aiFeedback);
        kpi.setBonusAmount(bonus);

        KPI savedKpi = kpiRepository.save(kpi);

        return mapToResponse(savedKpi, (int) totalTasks, (int) completedTasks, daysWorked, totalLateMinutes);
    }

    /**
     - 2. Lấy thông tin KPI của 1 nhân viên trong tháng cụ thể (Repo method 1)

     */
    public KPISummaryResponse getKPIByEmployeeAndMonth(Long employeeId, String monthYear) {
        Optional<KPI> kpiOpt = kpiRepository.findByEmployeeIdAndMonthYear(employeeId, monthYear);
        if (kpiOpt.isEmpty()) {
            KPISummaryResponse response = new KPISummaryResponse();
            response.setAiFeedback("Chưa có dữ liệu KPI cho tháng " + monthYear);
            return response;
        }

        YearMonth ym = YearMonth.parse(monthYear);
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        long totalTasks = taskRepository.countByEmployeeIdAndAssignedDateBetween(employeeId, startDate, endDate);
        long completedTasks = taskRepository.countByEmployeeIdAndCompletedAndAssignedDateBetween(employeeId, true, startDate, endDate);
        List<Attendance> attendances = attendanceRepository.findByEmployeeIdAndWorkDateBetween(employeeId, startDate, endDate);
        int totalLateMinutes = attendances.stream().mapToInt(a -> a.getLateMinutes() != null ? a.getLateMinutes() : 0).sum();

        return mapToResponse(kpiOpt.get(), (int) totalTasks, (int) completedTasks, attendances.size(), totalLateMinutes);
    }

    /**
     - 3. Xem lịch sử KPI của 1 nhân viên (sắp xếp mới nhất) (Repo method 2)

     */
    public List<KPISummaryResponse> getKPIHistoryByEmployee(Long employeeId) {
        return kpiRepository.findByEmployeeIdOrderByMonthYearDesc(employeeId)
                .stream()
                .map(kpi -> mapToResponse(kpi, 0, 0, 0, 0))
                .collect(Collectors.toList());
    }

    /**
     - 4. Lấy toàn bộ báo cáo KPI công ty trong 1 tháng (Dùng cho Admin/Manager) (Repo method 3)

     */
    public List<KPISummaryResponse> getCompanyKPIByMonth(String monthYear) {
        return kpiRepository.findByMonthYear(monthYear)
                .stream()
                .map(kpi -> mapToResponse(kpi, 0, 0, 0, 0))
                .collect(Collectors.toList());
    }

    // Helper Map DTO
    private KPISummaryResponse mapToResponse(KPI kpi, int totalTasks, int completedTasks, int daysWorked, int totalLateMinutes) {
        KPISummaryResponse res = new KPISummaryResponse();
        res.setEmployeeId(kpi.getEmployee().getId());
        res.setEmployeeName(kpi.getEmployee().getFullName());
        res.setMonthYear(kpi.getMonthYear());
        res.setCompletionRate(kpi.getAchievedKpi());
        res.setAiFeedback(kpi.getAiFeedback());
        res.setBonusAmount(kpi.getBonusAmount());
        res.setTotalTasks(totalTasks);
        res.setCompletedTasks(completedTasks);
        res.setDaysWorked(daysWorked);
        res.setTotalLateMinutes(totalLateMinutes);
        return res;
    }
}