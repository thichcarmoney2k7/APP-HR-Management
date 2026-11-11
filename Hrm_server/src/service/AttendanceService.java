//Lưu lượt chấm công
package service;

import dto.request.FaceCheckinRequest;
import dto.response.AttendanceResponse;
import entity.Attendance;
import entity.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.AttendanceRepository;
import repository.EmployeeRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    // Quy định giờ làm việc chuẩn của công ty
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
    public AttendanceResponse checkIn(FaceCheckinRequest request) {
        // Tìm thông tin nhân viên
        Optional<Employee> employeeOpt = employeeRepository.findById(request.getEmployeeId());
        if (employeeOpt.isEmpty()) {
            return buildErrorResponse("Không tìm thấy thông tin nhân viên với ID: " + request.getEmployeeId());
        }
        Employee employee = employeeOpt.get();

        // Xác thực khuôn mặt qua FaceRecognitionService
        boolean isFaceValid = faceRecognitionService.verifyFace(employee.getFaceId(), request.getFaceData());
        if (!isFaceValid) {
            return buildErrorResponse("Xác thực khuôn mặt không hợp lệ!");
        }

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // Kiểm tra xem hôm nay nhân viên đã check-in chưa
        Optional<Attendance> existingAttendance = attendanceRepository.findByEmployeeIdAndWorkDate(employee.getId(), today);
        if (existingAttendance.isPresent() && existingAttendance.get().getCheckIn() != null) {
            return buildErrorResponse("Nhân viên " + employee.getFullName() + " đã check-in ngày hôm nay rồi!");
        }

        // Tính phút đi muộn (nếu check-in sau 08:00)
        int lateMinutes = 0;
        String status = "PRESENT";
        if (now.isAfter(WORK_START_TIME)) {
            lateMinutes = (int) Duration.between(WORK_START_TIME, now).toMinutes();
            status = "LATE";
        }

        // Khởi tạo hoặc cập nhật bản ghi Attendance
        Attendance attendance = existingAttendance.orElse(new Attendance());
        attendance.setEmployeeId(employee.getId());
        attendance.setWorkDate(today);
        attendance.setCheckIn(now);
        attendance.setLateMinutes(lateMinutes);
        attendance.setStatus(status);

        Attendance saved = attendanceRepository.save(attendance);
        return mapToResponse(saved, employee.getFullName(), "Check-in thành công!");
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

        // Tìm bản ghi check-in trong ngày hôm nay
        Optional<Attendance> attendanceOpt = attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, today);
        if (attendanceOpt.isEmpty() || attendanceOpt.get().getCheckIn() == null) {
            return buildErrorResponse("Bạn chưa check-in ngày hôm nay nên không thể check-out!");
        }

        Attendance attendance = attendanceOpt.get();
        attendance.setCheckOut(now);

        // Tính phút về sớm (nếu check-out trước 17:00)
        int earlyLeaveMinutes = 0;
        if (now.isBefore(WORK_END_TIME)) {
            earlyLeaveMinutes = (int) Duration.between(now, WORK_END_TIME).toMinutes();
        }
        attendance.setEarlyLeaveMinutes(earlyLeaveMinutes);

        // Tính tổng số giờ làm việc trong ngày (quy đổi ra giờ, ví dụ: 8.5 giờ)
        long durationMinutes = Duration.between(attendance.getCheckIn(), now).toMinutes();
        double workHours = Math.round((durationMinutes / 60.0) * 100.0) / 100.0;
        attendance.setWorkHours(workHours);

        Attendance saved = attendanceRepository.save(attendance);
        return mapToResponse(saved, employee.getFullName(), "Check-out thành công!");
    }

    /**
     * 3. Lấy lịch sử chấm công của 1 nhân viên
     */
    public List<AttendanceResponse> getEmployeeAttendanceHistory(Long employeeId) {
        Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
        String employeeName = employeeOpt.map(Employee::getFullName).orElse("Unknown");

        return attendanceRepository.findByEmployeeIdOrderByWorkDateDesc(employeeId)
                .stream()
                .map(att -> mapToResponse(att, employeeName, "Thành công"))
                .collect(Collectors.toList());
    }

    // --- Helper Methods ---

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
        response.setStatus(attendance.getStatus());
        response.setMessage(message);
        return response;
    }

    private AttendanceResponse buildErrorResponse(String message) {
        AttendanceResponse response = new AttendanceResponse();
        response.setMessage(message);
        return response;
    }
}