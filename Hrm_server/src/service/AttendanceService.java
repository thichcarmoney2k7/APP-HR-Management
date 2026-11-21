package service;

import dto.request.FaceCheckinRequest;
import dto.response.AttendanceResponse;
import entity.Attendance;
import entity.Employee;
import enums.AttendanceStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.AttendanceRepository;
import repository.EmployeeRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    private static final LocalTime WORK_START_TIME = LocalTime.of(8, 0);   // 08:00 AM
    private static final LocalTime WORK_END_TIME = LocalTime.of(17, 0);    // 17:00 PM

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;
    private final FaceRecognitionService faceRecognitionService;

    @Autowired
    public AttendanceService(AttendanceRepository attendanceRepository,
                             EmployeeRepository employeeRepository,
                             FaceRecognitionService faceRecognitionService) {
        this.attendanceRepository = attendanceRepository;
        this.employeeRepository = employeeRepository;
        this.faceRecognitionService = faceRecognitionService;
    }

    /**
     * 1. Xử lý Check-in bằng Khuôn mặt (Face ID)
     */
    // Nhớ thêm dòng import này ở đầu file AttendanceService.java:
// import enums.AttendanceStatus;

    public AttendanceResponse checkIn(FaceCheckinRequest request) {
        Optional<Employee> employeeOpt = employeeRepository.findById(request.getEmployeeId());
        if (employeeOpt.isEmpty()) {
            return buildErrorResponse("Không tìm thấy thông tin nhân viên với ID: " + request.getEmployeeId());
        }
        Employee employee = employeeOpt.get();

        // 1. Kiểm tra nhân viên đã đăng ký dữ liệu khuôn mặt mẫu chưa
        if (employee.getFaceId() == null || employee.getFaceId().isBlank()) {
            return buildErrorResponse("Nhân viên chưa đăng ký dữ liệu khuôn mặt! Vui lòng đăng ký Face ID trước.");
        }

        // 2. Xác thực khuôn mặt qua FaceRecognitionService
        boolean isFaceValid = faceRecognitionService.verifyFace(employee.getFaceId(), request.getFaceData());
        if (!isFaceValid) {
            return buildErrorResponse("Xác thực khuôn mặt không hợp lệ!");
        }

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // 3. Kiểm tra đã check-in hôm nay chưa
        Optional<Attendance> existingAttendance = attendanceRepository.findByEmployeeIdAndWorkDate(employee.getId(), today);
        if (existingAttendance.isPresent() && existingAttendance.get().getCheckIn() != null) {
            return buildErrorResponse("Nhân viên " + employee.getFullName() + " đã check-in ngày hôm nay rồi!");
        }

        // 4. Tính phút đi muộn & gán trạng thái bằng Enum AttendanceStatus
        int lateMinutes = 0;
        AttendanceStatus status = AttendanceStatus.ON_TIME;

        if (now.isAfter(WORK_START_TIME)) {
            lateMinutes = (int) Duration.between(WORK_START_TIME, now).toMinutes();
            status = AttendanceStatus.LATE;
        }

        // 5. Lưu thông tin check-in vào Database
        Attendance attendance = existingAttendance.orElse(new Attendance());
        attendance.setEmployeeId(employee.getId());
        attendance.setWorkDate(today);
        attendance.setCheckIn(now);
        attendance.setLateMinutes(lateMinutes);
        attendance.setStatus(status);

        Attendance saved = attendanceRepository.save(attendance);

        String message = (status == AttendanceStatus.LATE)
                ? "Check-in thành công (Đi muộn " + lateMinutes + " phút)!"
                : "Check-in đúng giờ thành công!";

        return mapToResponse(saved, employee.getFullName(), message);
    }

    /**
     * 2. Xử lý Check-out kết thúc ca làm việc
     */
    public AttendanceResponse checkOut(Long employeeId) {
        Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
        if (employeeOpt.isEmpty()) {
            return buildErrorResponse("Không tìm thấy thông tin nhân viên!");
        }
        Employee employee = employeeOpt.get();

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        Optional<Attendance> attendanceOpt = attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, today);
        if (attendanceOpt.isEmpty() || attendanceOpt.get().getCheckIn() == null) {
            return buildErrorResponse("Bạn chưa check-in ngày hôm nay nên không thể check-out!");
        }

        Attendance attendance = attendanceOpt.get();

        // Chặn check-out 2 lần trong ngày
        if (attendance.getCheckOut() != null) {
            return buildErrorResponse("Bạn đã check-out ngày hôm nay lúc " + attendance.getCheckOut() + " rồi!");
        }

        attendance.setCheckOut(now);

        // Tính phút về sớm (nếu check-out trước 17:00)
        int earlyLeaveMinutes = 0;
        if (now.isBefore(WORK_END_TIME)) {
            earlyLeaveMinutes = (int) Duration.between(now, WORK_END_TIME).toMinutes();
        }
        attendance.setEarlyLeaveMinutes(earlyLeaveMinutes);

        // Tính tổng số giờ làm việc trong ngày
        long durationMinutes = Duration.between(attendance.getCheckIn(), now).toMinutes();
        double workHours = Math.max(0.0, Math.round((durationMinutes / 60.0) * 100.0) / 100.0);
        attendance.setWorkHours(workHours);

        Attendance saved = attendanceRepository.save(attendance);
        return mapToResponse(saved, employee.getFullName(), "Check-out thành công!");
    }

    /**
     * 3. Kiểm tra nhanh hôm nay nhân viên đã chấm công chưa (Repo method 4)
     */
    public boolean hasCheckedInToday(Long employeeId) {
        return attendanceRepository.existsByEmployeeIdAndWorkDate(employeeId, LocalDate.now());
    }

    /**
     * 4. Lấy chi tiết bản ghi chấm công hôm nay của 1 nhân viên (Phục vụ hiển thị lên UI JavaFX)
     */
    public AttendanceResponse getTodayAttendance(Long employeeId) {
        String employeeName = getEmployeeName(employeeId);
        return attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, LocalDate.now())
                .map(att -> mapToResponse(att, employeeName, "Đã chấm công hôm nay"))
                .orElseGet(() -> buildErrorResponse("Hôm nay chưa có dữ liệu chấm công"));
    }

    /**
     * 5. Lấy toàn bộ lịch sử chấm công của 1 nhân viên (Repo method 2)
     */
    public List<AttendanceResponse> getEmployeeAttendanceHistory(Long employeeId) {
        String employeeName = getEmployeeName(employeeId);
        return attendanceRepository.findByEmployeeIdOrderByWorkDateDesc(employeeId)
                .stream()
                .map(att -> mapToResponse(att, employeeName, "Thành công"))
                .collect(Collectors.toList());
    }

    /**
     * 6. Lấy bảng công theo tháng của 1 nhân viên, định dạng monthYear: "2026-11" (Repo method 3)
     */
    public List<AttendanceResponse> getMonthlyAttendance(Long employeeId, String monthYear) {
        YearMonth ym = YearMonth.parse(monthYear);
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();
        String employeeName = getEmployeeName(employeeId);

        return attendanceRepository.findByEmployeeIdAndWorkDateBetween(employeeId, startDate, endDate)
                .stream()
                .map(att -> mapToResponse(att, employeeName, "Thành công"))
                .collect(Collectors.toList());
    }

    /**
     * 7. Lấy danh sách chấm công toàn bộ nhân viên theo ngày (Repo method 5 - Dành cho Manager/HR)
     */
    public List<AttendanceResponse> getAllAttendanceByDate(LocalDate date) {
        return attendanceRepository.findByWorkDate(date)
                .stream()
                .map(att -> mapToResponse(att, getEmployeeName(att.getEmployeeId()), "Thành công"))
                .collect(Collectors.toList());
    }

    // --- Helper Methods ---

    private String getEmployeeName(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .map(Employee::getFullName)
                .orElse("Unknown");
    }

    private AttendanceResponse mapToResponse(Attendance attendance, String employeeName, String message) {
        AttendanceResponse response = new AttendanceResponse();
        response.setAttendanceId(attendance.getId());
        response.setEmployeeId(attendance.getEmployeeId());
        response.setEmployeeName(employeeName);
        response.setWorkDate(attendance.getWorkDate());
        response.setCheckInTime(attendance.getCheckIn());
        response.setCheckOutTime(attendance.getCheckOut());
        response.setLateMinutes(attendance.getLateMinutes() != null ? attendance.getLateMinutes() : 0);
        response.setWorkHours(attendance.getWorkHours() != null ? attendance.getWorkHours() : 0.0);
        response.setStatus(attendance.getStatus() != null ? attendance.getStatus().name() : null);
        response.setMessage(message);
        return response;
    }

    private AttendanceResponse buildErrorResponse(String message) {
        AttendanceResponse response = new AttendanceResponse();
        response.setMessage(message);
        return response;
    }
}

